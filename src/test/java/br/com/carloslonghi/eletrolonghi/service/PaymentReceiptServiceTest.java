package br.com.carloslonghi.eletrolonghi.service;

import br.com.carloslonghi.eletrolonghi.config.ShopProperties;
import br.com.carloslonghi.eletrolonghi.entity.Accessory;
import br.com.carloslonghi.eletrolonghi.entity.Payment;
import br.com.carloslonghi.eletrolonghi.entity.RepairOrder;
import br.com.carloslonghi.eletrolonghi.entity.enums.PaymentMethod;
import br.com.carloslonghi.eletrolonghi.entity.enums.PaymentStatus;
import br.com.carloslonghi.eletrolonghi.support.TestFixtures;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentReceiptServiceTest {

    private final PaymentReceiptService service = new PaymentReceiptService(
            new ShopProperties("Eletro Longhi", "12.345.678/0001-90", "Rua A, 100", "11 4000-0000", "loja@eletrolonghi.com", 90));

    @Test
    void shouldGeneratePdfBytes() {
        Payment payment = TestFixtures.payment(1L);
        payment.setDescription("Entrada do serviço");
        payment.setPayerName("João da Silva");
        payment.setPayerDocument("123.456.789-00");

        byte[] pdf = service.generate(payment);

        assertThat(pdf).isNotEmpty();
        assertThat(new String(pdf, 0, 5, StandardCharsets.ISO_8859_1)).isEqualTo("%PDF-");
    }

    @Test
    void shouldGeneratePdfForInstallmentCardPayment() {
        Payment payment = TestFixtures.payment(2L);
        payment.setMethod(PaymentMethod.CARD);
        payment.setInstallments(3);
        payment.setStatus(PaymentStatus.APPROVED);
        payment.setPaidAt(LocalDateTime.now());

        String text = textOf(service.generate(payment));

        assertThat(text).contains("3x");
    }

    @Test
    void shouldGeneratePdfWhenShopDataMissing() {
        PaymentReceiptService bareService = new PaymentReceiptService(
                new ShopProperties(null, null, null, null, null, null));

        byte[] pdf = bareService.generate(TestFixtures.payment(3L));

        assertThat(pdf).isNotEmpty();
    }

    @Test
    void shouldIncludeCustomerRepairOrderAndDeviceDetails() {
        Payment payment = TestFixtures.payment(4L);
        RepairOrder order = payment.getRepairOrder();
        order.setEstimatedCost(new BigDecimal("150.00"));
        order.setEstimatedCompletionDate(LocalDate.of(2026, 11, 5));
        order.setDescription("Troca da tela");

        String text = textOf(service.generate(payment));

        assertThat(text)
                .contains("Cliente 1", "11999990001", "cliente1@mail.com")
                .contains("Marca 1 Modelo 1", "SERIAL-1", "Acessorio 1")
                .contains("05/11/2026", "R$ 150,00", "R$ 100,00")
                .contains("Troca da tela")
                .contains("Assinatura do cliente");
    }

    @Test
    void shouldFormatMoneyInBrazilianFormat() {
        assertThat(PaymentReceiptService.formatMoney(new BigDecimal("1250.5"))).isEqualTo("R$ 1.250,50");
        assertThat(PaymentReceiptService.formatMoney(new BigDecimal("80"))).isEqualTo("R$ 80,00");
        assertThat(PaymentReceiptService.formatMoney(null)).isEqualTo("-");
    }

    @Test
    void shouldPrintWarrantyOnlyForApprovedPayment() {
        Payment pending = TestFixtures.payment(5L);
        assertThat(textOf(service.generate(pending))).doesNotContain("Garantia");

        Payment approved = TestFixtures.payment(6L);
        approved.setStatus(PaymentStatus.APPROVED);
        approved.setPaidAt(LocalDateTime.of(2026, 10, 1, 10, 0));

        assertThat(textOf(service.generate(approved))).contains("Garantia de 90 dias", "30/12/2026");
    }

    @Test
    void shouldOmitWarrantyWhenDisabled() {
        PaymentReceiptService noWarranty = new PaymentReceiptService(
                new ShopProperties("Eletro Longhi", null, null, null, null, 0));
        Payment approved = TestFixtures.payment(7L);
        approved.setStatus(PaymentStatus.APPROVED);
        approved.setPaidAt(LocalDateTime.now());

        assertThat(textOf(noWarranty.generate(approved))).doesNotContain("Garantia");
    }

    @Test
    void shouldStayOnOnePageWithLongTextsAndManyAccessories() throws IOException {
        Payment payment = TestFixtures.payment(8L);
        payment.setStatus(PaymentStatus.APPROVED);
        payment.setPaidAt(LocalDateTime.now());
        payment.setPayerName("João da Silva");
        payment.setPayerDocument("12345678900");
        payment.setGatewayPaymentId("123456789");
        payment.setDescription("x".repeat(2000));
        RepairOrder order = payment.getRepairOrder();
        order.setDescription("Defeito relatado pelo cliente ".repeat(100));
        order.setEstimatedCost(new BigDecimal("150.00"));
        order.setEstimatedCompletionDate(LocalDate.now());
        List<Accessory> accessories = LongStream.rangeClosed(1, 15).mapToObj(TestFixtures::accessory).toList();
        order.getDevice().setAccessories(accessories);

        try (PdfReader reader = new PdfReader(service.generate(payment))) {
            assertThat(reader.getNumberOfPages()).isEqualTo(1);
        }
    }

    private static String textOf(byte[] pdf) {
        try (PdfReader reader = new PdfReader(pdf)) {
            return new PdfTextExtractor(reader).getTextFromPage(1);
        } catch (IOException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
