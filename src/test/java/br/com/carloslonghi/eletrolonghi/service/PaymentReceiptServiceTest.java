package br.com.carloslonghi.eletrolonghi.service;

import br.com.carloslonghi.eletrolonghi.config.ShopProperties;
import br.com.carloslonghi.eletrolonghi.entity.Payment;
import br.com.carloslonghi.eletrolonghi.entity.enums.PaymentMethod;
import br.com.carloslonghi.eletrolonghi.entity.enums.PaymentStatus;
import br.com.carloslonghi.eletrolonghi.support.TestFixtures;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentReceiptServiceTest {

    private final PaymentReceiptService service = new PaymentReceiptService(
            new ShopProperties("Eletro Longhi", "12.345.678/0001-90", "Rua A, 100", "11 4000-0000", "loja@eletrolonghi.com"));

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
        payment.setPaidAt(java.time.LocalDateTime.now());

        byte[] pdf = service.generate(payment);

        assertThat(pdf).isNotEmpty();
        assertThat(new String(pdf, 0, 5, StandardCharsets.ISO_8859_1)).isEqualTo("%PDF-");
    }

    @Test
    void shouldGeneratePdfWhenShopDataMissing() {
        PaymentReceiptService bareService = new PaymentReceiptService(
                new ShopProperties(null, null, null, null, null));

        byte[] pdf = bareService.generate(TestFixtures.payment(3L));

        assertThat(pdf).isNotEmpty();
    }
}
