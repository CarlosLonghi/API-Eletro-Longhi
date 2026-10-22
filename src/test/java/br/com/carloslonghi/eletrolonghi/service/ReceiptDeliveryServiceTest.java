package br.com.carloslonghi.eletrolonghi.service;

import br.com.carloslonghi.eletrolonghi.config.ShopProperties;
import br.com.carloslonghi.eletrolonghi.entity.Payment;
import br.com.carloslonghi.eletrolonghi.entity.enums.PaymentStatus;
import br.com.carloslonghi.eletrolonghi.exception.CustomerContactMissingException;
import br.com.carloslonghi.eletrolonghi.exception.ReceiptDeliveryException;
import br.com.carloslonghi.eletrolonghi.metrics.PaymentMetrics;
import br.com.carloslonghi.eletrolonghi.repository.PaymentRepository;
import br.com.carloslonghi.eletrolonghi.service.mail.ReceiptMailer;
import br.com.carloslonghi.eletrolonghi.support.TestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReceiptDeliveryServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentReceiptService paymentReceiptService;

    @Mock
    private ReceiptMailer receiptMailer;

    @Mock
    private PaymentMetrics paymentMetrics;

    private ReceiptDeliveryService service;

    private final byte[] pdf = "%PDF-1.4 fake".getBytes();

    @BeforeEach
    void setUp() {
        ShopProperties shop = new ShopProperties("Eletro Longhi", null, null, null, null, 90);
        service = new ReceiptDeliveryService(paymentRepository, paymentReceiptService, receiptMailer, shop, paymentMetrics);
    }

    @Test
    void shouldRenderReceiptWhenPaymentExists() {
        Payment payment = TestFixtures.payment(1L);
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(paymentReceiptService.generate(payment)).thenReturn(pdf);

        assertThat(service.renderReceipt(1L)).contains(pdf);
    }

    @Test
    void shouldReturnEmptyWhenPaymentMissing() {
        when(paymentRepository.findById(9L)).thenReturn(Optional.empty());

        assertThat(service.renderReceipt(9L)).isEmpty();
        assertThat(service.sendReceiptByEmail(9L)).isEmpty();
        assertThat(service.whatsAppLink(9L)).isEmpty();
        verifyNoInteractions(receiptMailer);
    }

    @Test
    void shouldEmailReceiptToCustomerWithPdfAttached() {
        Payment payment = TestFixtures.payment(1L);
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(paymentReceiptService.generate(payment)).thenReturn(pdf);
        when(paymentReceiptService.warrantyUntil(payment)).thenReturn(Optional.empty());

        assertThat(service.sendReceiptByEmail(1L)).contains(payment);

        ArgumentCaptor<String> subject = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> body = ArgumentCaptor.forClass(String.class);
        verify(receiptMailer).send(eq("cliente1@mail.com"), subject.capture(), body.capture(),
                eq("comprovante-1.pdf"), eq(pdf));
        assertThat(subject.getValue()).contains("OS nº 1", "Eletro Longhi");
        assertThat(body.getValue()).contains("Cliente", "OS nº 1", "Marca 1 Modelo 1", "R$ 100,00");
        verify(paymentMetrics).receiptSent(PaymentMetrics.CHANNEL_EMAIL, true);
    }

    @Test
    void shouldEscapeCustomerDataInEmailBody() {
        Payment payment = TestFixtures.payment(1L);
        payment.getRepairOrder().getCustomer().setName("<b>Ana</b> Souza");
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(paymentReceiptService.generate(payment)).thenReturn(pdf);
        when(paymentReceiptService.warrantyUntil(payment)).thenReturn(Optional.empty());

        service.sendReceiptByEmail(1L);

        ArgumentCaptor<String> body = ArgumentCaptor.forClass(String.class);
        verify(receiptMailer).send(anyString(), anyString(), body.capture(), anyString(), any());
        assertThat(body.getValue()).contains("&lt;b&gt;Ana&lt;/b&gt;").doesNotContain("<b>Ana");
    }

    @Test
    void shouldIncludeWarrantyInEmailWhenApproved() {
        Payment payment = TestFixtures.payment(1L);
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(paymentReceiptService.generate(payment)).thenReturn(pdf);
        when(paymentReceiptService.warrantyUntil(payment)).thenReturn(Optional.of(java.time.LocalDate.of(2027, 1, 20)));

        service.sendReceiptByEmail(1L);

        ArgumentCaptor<String> body = ArgumentCaptor.forClass(String.class);
        verify(receiptMailer).send(anyString(), anyString(), body.capture(), anyString(), any());
        assertThat(body.getValue()).contains("90 dias", "20/01/2027");
    }

    @Test
    void shouldRejectEmailWhenCustomerHasNoEmail() {
        Payment payment = TestFixtures.payment(1L);
        payment.getRepairOrder().getCustomer().setEmail(" ");
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        assertThatThrownBy(() -> service.sendReceiptByEmail(1L))
                .isInstanceOf(CustomerContactMissingException.class)
                .hasMessageContaining("e-mail");
        verifyNoInteractions(receiptMailer);
    }

    @Test
    void shouldCountFailureAndRethrowWhenMailerFails() {
        Payment payment = TestFixtures.payment(1L);
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(paymentReceiptService.generate(payment)).thenReturn(pdf);
        when(paymentReceiptService.warrantyUntil(payment)).thenReturn(Optional.empty());
        doThrow(new ReceiptDeliveryException("smtp fora", new RuntimeException()))
                .when(receiptMailer).send(anyString(), anyString(), anyString(), anyString(), any());

        assertThatThrownBy(() -> service.sendReceiptByEmail(1L)).isInstanceOf(ReceiptDeliveryException.class);
        verify(paymentMetrics).receiptSent(PaymentMetrics.CHANNEL_EMAIL, false);
        verify(paymentMetrics, never()).receiptSent(PaymentMetrics.CHANNEL_EMAIL, true);
    }

    @Test
    void shouldBuildWhatsAppLinkWithEncodedMessage() {
        Payment payment = TestFixtures.payment(1L);
        payment.setStatus(PaymentStatus.APPROVED);
        payment.setPaidAt(LocalDateTime.of(2026, 10, 22, 16, 0));
        payment.getRepairOrder().getCustomer().setName("Maria Oliveira");
        payment.getRepairOrder().getCustomer().setPhone("(11) 98765-4321");
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(paymentReceiptService.warrantyUntil(payment)).thenReturn(Optional.of(java.time.LocalDate.of(2027, 1, 20)));

        ReceiptDeliveryService.WhatsAppLink link = service.whatsAppLink(1L).orElseThrow();

        assertThat(link.phone()).isEqualTo("5511987654321");
        assertThat(link.message())
                .startsWith("Olá, Maria! Aqui é da Eletro Longhi.")
                .contains("*OS nº 1*", "Marca 1 Modelo 1", "R$ 100,00", "Aprovado em 22/10/2026", "90 dias, até 20/01/2027");
        assertThat(link.url()).startsWith("https://wa.me/5511987654321?text=").doesNotContain(" ", "+");
        String decoded = URLDecoder.decode(link.url().substring(link.url().indexOf("=") + 1), StandardCharsets.UTF_8);
        assertThat(decoded).isEqualTo(link.message());
        verify(paymentMetrics).receiptSent(PaymentMetrics.CHANNEL_WHATSAPP, true);
    }

    @Test
    void shouldRejectWhatsAppWhenPhoneInvalid() {
        Payment payment = TestFixtures.payment(1L);
        payment.getRepairOrder().getCustomer().setPhone("4000-0000");
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        assertThatThrownBy(() -> service.whatsAppLink(1L))
                .isInstanceOf(CustomerContactMissingException.class)
                .hasMessageContaining("telefone");
    }

    @Test
    void shouldNormalizeBrazilianPhones() {
        assertThat(ReceiptDeliveryService.normalizePhone("(11) 98765-4321")).contains("5511987654321");
        assertThat(ReceiptDeliveryService.normalizePhone("11 4000-0000")).contains("551140000000");
        assertThat(ReceiptDeliveryService.normalizePhone("+55 11 98765-4321")).contains("5511987654321");
        assertThat(ReceiptDeliveryService.normalizePhone("98765-4321")).isEmpty();
        assertThat(ReceiptDeliveryService.normalizePhone("+1 202 555 0100 99")).isEmpty();
        assertThat(ReceiptDeliveryService.normalizePhone(null)).isEmpty();
    }
}
