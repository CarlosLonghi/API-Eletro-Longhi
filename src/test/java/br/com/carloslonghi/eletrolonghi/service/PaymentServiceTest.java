package br.com.carloslonghi.eletrolonghi.service;

import br.com.carloslonghi.eletrolonghi.client.MercadoPagoClient;
import br.com.carloslonghi.eletrolonghi.client.dto.CheckoutPreference;
import br.com.carloslonghi.eletrolonghi.client.dto.GatewayPaymentSnapshot;
import br.com.carloslonghi.eletrolonghi.client.dto.PreferencePayer;
import br.com.carloslonghi.eletrolonghi.entity.Payment;
import br.com.carloslonghi.eletrolonghi.entity.enums.PaymentMethod;
import br.com.carloslonghi.eletrolonghi.entity.enums.PaymentStatus;
import br.com.carloslonghi.eletrolonghi.entity.enums.RepairOrderStatus;
import br.com.carloslonghi.eletrolonghi.exception.InvalidPaymentCheckoutException;
import br.com.carloslonghi.eletrolonghi.exception.PaymentAlreadyExistsForRepairOrderException;
import br.com.carloslonghi.eletrolonghi.exception.PaymentGatewayException;
import br.com.carloslonghi.eletrolonghi.exception.ReferencedEntityNotFoundException;
import br.com.carloslonghi.eletrolonghi.exception.RepairOrderNotApprovedForPaymentException;
import br.com.carloslonghi.eletrolonghi.repository.PaymentRepository;
import br.com.carloslonghi.eletrolonghi.support.TestFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private RepairOrderService repairOrderService;

    @Mock
    private MercadoPagoClient mercadoPagoClient;

    @InjectMocks
    private PaymentService paymentService;

    private static Payment checkoutPayment() {
        Payment payment = TestFixtures.payment(1L);
        payment.setMethod(PaymentMethod.MERCADO_PAGO_CHECKOUT);
        return payment;
    }

    /** Pagamento cuja ordem de reparo já teve o orçamento aprovado — pré-requisito do save. */
    private static Payment paymentForApprovedOrder() {
        Payment payment = TestFixtures.payment(1L);
        payment.getRepairOrder().setStatus(RepairOrderStatus.APPROVED);
        return payment;
    }

    @Test
    void shouldFindWithFilters() {
        Page<Payment> page = new PageImpl<>(List.of(TestFixtures.payment(1L)));
        when(paymentRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(PageRequest.class)))
                .thenReturn(page);

        Page<Payment> result = paymentService.findAll(
                PaymentStatus.PENDING, PaymentMethod.CASH, 1L, null, null, PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void shouldSavePendingPaymentAndResolveRepairOrder() {
        Payment payment = paymentForApprovedOrder();
        when(repairOrderService.findById(1L)).thenReturn(Optional.of(payment.getRepairOrder()));
        when(paymentRepository.existsByRepairOrderId(1L)).thenReturn(false);
        when(paymentRepository.save(payment)).thenReturn(payment);

        Payment saved = paymentService.save(payment);

        assertThat(saved.getStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(saved.getPaidAt()).isNull();
    }

    @Test
    void shouldThrowWhenRepairOrderMissingOnSave() {
        Payment payment = TestFixtures.payment(1L);
        when(repairOrderService.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.save(payment))
                .isInstanceOf(ReferencedEntityNotFoundException.class);
    }

    @Test
    void shouldRejectPaymentWhenRepairOrderBudgetNotYetApproved() {
        Payment payment = TestFixtures.payment(1L);
        payment.getRepairOrder().setStatus(RepairOrderStatus.AWAITING_APPROVAL);
        when(repairOrderService.findById(1L)).thenReturn(Optional.of(payment.getRepairOrder()));

        assertThatThrownBy(() -> paymentService.save(payment))
                .isInstanceOf(RepairOrderNotApprovedForPaymentException.class);

        verify(paymentRepository, org.mockito.Mockito.never()).save(any(Payment.class));
    }

    @Test
    void shouldAllowPaymentOnceRepairOrderMovedPastApproval() {
        Payment payment = TestFixtures.payment(1L);
        payment.getRepairOrder().setStatus(RepairOrderStatus.IN_REPAIR);
        when(repairOrderService.findById(1L)).thenReturn(Optional.of(payment.getRepairOrder()));
        when(paymentRepository.existsByRepairOrderId(1L)).thenReturn(false);
        when(paymentRepository.save(payment)).thenReturn(payment);

        assertThat(paymentService.save(payment)).isSameAs(payment);
    }

    @Test
    void shouldThrowWhenRepairOrderAlreadyHasPayment() {
        Payment payment = paymentForApprovedOrder();
        when(repairOrderService.findById(1L)).thenReturn(Optional.of(payment.getRepairOrder()));
        when(paymentRepository.existsByRepairOrderId(1L)).thenReturn(true);

        assertThatThrownBy(() -> paymentService.save(payment))
                .isInstanceOf(PaymentAlreadyExistsForRepairOrderException.class);
    }

    @Test
    void shouldNormalizeInstallmentsForNonCardMethod() {
        Payment payment = paymentForApprovedOrder();
        payment.setMethod(PaymentMethod.PIX);
        payment.setInstallments(6);
        when(repairOrderService.findById(1L)).thenReturn(Optional.of(payment.getRepairOrder()));
        when(paymentRepository.existsByRepairOrderId(1L)).thenReturn(false);
        when(paymentRepository.save(payment)).thenReturn(payment);

        paymentService.save(payment);

        assertThat(payment.getInstallments()).isEqualTo(1);
    }

    @Test
    void shouldApproveOnSaveAndStampPaidAt() {
        Payment payment = paymentForApprovedOrder();
        payment.setStatus(PaymentStatus.APPROVED);
        when(repairOrderService.findById(1L)).thenReturn(Optional.of(payment.getRepairOrder()));
        when(paymentRepository.existsByRepairOrderId(1L)).thenReturn(false);
        when(paymentRepository.save(payment)).thenReturn(payment);

        paymentService.save(payment);

        assertThat(payment.getPaidAt()).isNotNull();
    }

    @Test
    void shouldStampPaidAtWhenStatusBecomesApproved() {
        Payment payment = TestFixtures.payment(1L);
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(payment)).thenReturn(payment);

        Optional<Payment> updated = paymentService.updateStatus(1L, PaymentStatus.APPROVED);

        assertThat(updated).isPresent();
        assertThat(payment.getPaidAt()).isNotNull();
    }

    @Test
    void shouldNotRestampPaidAtWhenAlreadyPaid() {
        Payment payment = TestFixtures.payment(1L);
        payment.setStatus(PaymentStatus.APPROVED);
        java.time.LocalDateTime paidAt = java.time.LocalDateTime.now().minusDays(1);
        payment.setPaidAt(paidAt);
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(payment)).thenReturn(payment);

        paymentService.updateStatus(1L, PaymentStatus.APPROVED);

        assertThat(payment.getPaidAt()).isEqualTo(paidAt);
    }

    @Test
    void shouldUpdatePaymentWhenFound() {
        Payment existing = TestFixtures.payment(1L);
        Payment incoming = TestFixtures.payment(2L);
        incoming.setMethod(PaymentMethod.CARD);
        incoming.setInstallments(4);
        incoming.setDescription("Parcelado");
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(paymentRepository.save(existing)).thenReturn(existing);

        Optional<Payment> updated = paymentService.update(1L, incoming);

        assertThat(updated).isPresent();
        assertThat(existing.getMethod()).isEqualTo(PaymentMethod.CARD);
        assertThat(existing.getInstallments()).isEqualTo(4);
        assertThat(existing.getDescription()).isEqualTo("Parcelado");
    }

    @Test
    void shouldReturnEmptyWhenUpdatingOrStatusMissing() {
        when(paymentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThat(paymentService.update(1L, TestFixtures.payment(1L))).isEmpty();
        assertThat(paymentService.updateStatus(1L, PaymentStatus.APPROVED)).isEmpty();
    }

    @Test
    void shouldCreateCheckoutLinkAndPersistAUniqueExternalReference() {
        Payment payment = checkoutPayment();
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(mercadoPagoClient.createCheckoutPreference(any(), any(), anyString(), any()))
                .thenReturn(Optional.of(new CheckoutPreference("pref-1", "https://mp/checkout", "https://mp/sandbox")));

        Optional<CheckoutPreference> result = paymentService.createCheckoutLink(1L);

        assertThat(result).map(CheckoutPreference::initPoint).contains("https://mp/checkout");

        ArgumentCaptor<String> refCaptor = ArgumentCaptor.forClass(String.class);
        verify(mercadoPagoClient).createCheckoutPreference(any(), any(), refCaptor.capture(), any());
        // não pode ser só "payment-<id>": ids se repetem entre resets do banco e um
        // pagamento aprovado antigo no Mercado Pago com a mesma referência marcaria
        // este como pago no sync sem ninguém ter pagado.
        assertThat(payment.getExternalReference())
                .isEqualTo(refCaptor.getValue())
                .matches("payment-1-[0-9a-f-]{36}");
        verify(paymentRepository).save(payment);
    }

    @Test
    void shouldReuseExternalReferenceWhenLinkIsRegenerated() {
        Payment payment = checkoutPayment();
        payment.setExternalReference("payment-1-existing-ref");
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(mercadoPagoClient.createCheckoutPreference(any(), any(), any(), any()))
                .thenReturn(Optional.of(new CheckoutPreference("pref-1", "https://mp/checkout", "https://mp/sandbox")));

        paymentService.createCheckoutLink(1L);

        verify(mercadoPagoClient).createCheckoutPreference(any(), any(), eq("payment-1-existing-ref"), any());
        assertThat(payment.getExternalReference()).isEqualTo("payment-1-existing-ref");
    }

    @Test
    void shouldSendPayerFromPaymentAndCustomerWhenCreatingCheckoutLink() {
        Payment payment = checkoutPayment();
        payment.setPayerName("Ana Silva Souza");
        payment.setPayerDocument("123.456.789-09");
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(mercadoPagoClient.createCheckoutPreference(any(), any(), any(), any()))
                .thenReturn(Optional.of(new CheckoutPreference("pref-1", "https://mp/checkout", "https://mp/sandbox")));

        paymentService.createCheckoutLink(1L);

        ArgumentCaptor<PreferencePayer> captor = ArgumentCaptor.forClass(PreferencePayer.class);
        verify(mercadoPagoClient).createCheckoutPreference(any(), any(), anyString(), captor.capture());
        PreferencePayer payer = captor.getValue();
        assertThat(payer.name()).isEqualTo("Ana");
        assertThat(payer.surname()).isEqualTo("Silva Souza");
        assertThat(payer.email()).isEqualTo("cliente1@mail.com");
        assertThat(payer.identification().type()).isEqualTo("CPF");
        assertThat(payer.identification().number()).isEqualTo("12345678909");
    }

    @Test
    void shouldFallBackToCustomerNameAndOmitBadDocumentInCheckoutPayer() {
        Payment payment = checkoutPayment(); // no payerName, no payerDocument
        payment.setPayerDocument("123");
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(mercadoPagoClient.createCheckoutPreference(any(), any(), any(), any()))
                .thenReturn(Optional.of(new CheckoutPreference("pref-1", "https://mp/checkout", "https://mp/sandbox")));

        paymentService.createCheckoutLink(1L);

        ArgumentCaptor<PreferencePayer> captor = ArgumentCaptor.forClass(PreferencePayer.class);
        verify(mercadoPagoClient).createCheckoutPreference(any(), any(), any(), captor.capture());
        PreferencePayer payer = captor.getValue();
        assertThat(payer.name()).isEqualTo("Cliente"); // from customer(1L) name "Cliente 1"
        assertThat(payer.identification()).isNull();
    }

    @Test
    void shouldRejectCheckoutLinkForNonCheckoutPayment() {
        Payment payment = TestFixtures.payment(1L); // CASH
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        assertThatThrownBy(() -> paymentService.createCheckoutLink(1L))
                .isInstanceOf(InvalidPaymentCheckoutException.class);
    }

    @Test
    void shouldRejectCheckoutLinkWhenPaymentNotPending() {
        Payment payment = checkoutPayment();
        payment.setStatus(PaymentStatus.APPROVED);
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        assertThatThrownBy(() -> paymentService.createCheckoutLink(1L))
                .isInstanceOf(InvalidPaymentCheckoutException.class);
    }

    @Test
    void shouldFailCheckoutLinkWhenGatewayUnavailable() {
        Payment payment = checkoutPayment();
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(mercadoPagoClient.createCheckoutPreference(any(), any(), any(), any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.createCheckoutLink(1L))
                .isInstanceOf(PaymentGatewayException.class);
    }

    @Test
    void shouldReturnEmptyWhenCreatingCheckoutLinkForMissingPayment() {
        when(paymentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThat(paymentService.createCheckoutLink(1L)).isEmpty();
    }

    @Test
    void shouldSyncApprovedGatewayPaymentAndStampPaidAt() {
        Payment payment = checkoutPayment();
        payment.setExternalReference("payment-1");
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(payment)).thenReturn(payment);
        when(mercadoPagoClient.findPaymentByExternalReference("payment-1"))
                .thenReturn(Optional.of(new GatewayPaymentSnapshot(999L, "approved", "accredited", "payment-1")));

        Optional<Payment> result = paymentService.syncWithGateway(1L);

        assertThat(result).isPresent();
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.APPROVED);
        assertThat(payment.getPaidAt()).isNotNull();
        assertThat(payment.getGatewayPaymentId()).isEqualTo("999");
    }

    @Test
    void shouldMapRejectedGatewayStatus() {
        Payment payment = checkoutPayment();
        payment.setExternalReference("payment-1");
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(payment)).thenReturn(payment);
        when(mercadoPagoClient.findPaymentByExternalReference("payment-1"))
                .thenReturn(Optional.of(new GatewayPaymentSnapshot(1L, "rejected", "cc_rejected", "payment-1")));

        paymentService.syncWithGateway(1L);

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REJECTED);
        assertThat(payment.getPaidAt()).isNull();
    }

    @Test
    void shouldIgnoreUnknownGatewayStatus() {
        Payment payment = checkoutPayment();
        payment.setExternalReference("payment-1");
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(payment)).thenReturn(payment);
        when(mercadoPagoClient.findPaymentByExternalReference("payment-1"))
                .thenReturn(Optional.of(new GatewayPaymentSnapshot(1L, "some_new_state", null, "payment-1")));

        paymentService.syncWithGateway(1L);

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING);
    }

    @Test
    void shouldLeavePaymentUnchangedWhenGatewayHasNoPaymentYet() {
        Payment payment = checkoutPayment();
        payment.setExternalReference("payment-1");
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(mercadoPagoClient.findPaymentByExternalReference("payment-1")).thenReturn(Optional.empty());

        Optional<Payment> result = paymentService.syncWithGateway(1L);

        assertThat(result).contains(payment);
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING);
    }

    @Test
    void shouldRejectSyncWhenPaymentHasNoCheckoutLink() {
        Payment payment = checkoutPayment(); // externalReference == null
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        assertThatThrownBy(() -> paymentService.syncWithGateway(1L))
                .isInstanceOf(InvalidPaymentCheckoutException.class);
    }

    @Test
    void shouldReturnEmptyWhenSyncingMissingPayment() {
        when(paymentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThat(paymentService.syncWithGateway(1L)).isEmpty();
    }

    @Test
    void shouldFindByIdAndDelete() {
        Payment payment = TestFixtures.repairOrderWithPayment(1L, PaymentStatus.PENDING).getPayment();
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        assertThat(payment.getRepairOrder().getPayment()).isSameAs(payment);

        paymentService.deleteById(1L);

        assertThat(payment.getRepairOrder().getPayment()).isNull();
        verify(paymentRepository).delete(payment);
    }

    @Test
    void deleteShouldDoNothingWhenPaymentMissing() {
        when(paymentRepository.findById(1L)).thenReturn(Optional.empty());

        paymentService.deleteById(1L);

        verify(paymentRepository, org.mockito.Mockito.never()).delete(org.mockito.ArgumentMatchers.any(Payment.class));
    }
}
