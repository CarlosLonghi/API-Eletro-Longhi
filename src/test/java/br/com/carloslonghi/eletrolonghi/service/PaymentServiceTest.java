package br.com.carloslonghi.eletrolonghi.service;

import br.com.carloslonghi.eletrolonghi.client.MercadoPagoClient;
import br.com.carloslonghi.eletrolonghi.client.dto.CheckoutPreference;
import br.com.carloslonghi.eletrolonghi.client.dto.GatewayPaymentSnapshot;
import br.com.carloslonghi.eletrolonghi.entity.Payment;
import br.com.carloslonghi.eletrolonghi.entity.enums.PaymentMethod;
import br.com.carloslonghi.eletrolonghi.entity.enums.PaymentStatus;
import br.com.carloslonghi.eletrolonghi.exception.InvalidPaymentCheckoutException;
import br.com.carloslonghi.eletrolonghi.exception.PaymentAlreadyExistsForRepairOrderException;
import br.com.carloslonghi.eletrolonghi.exception.PaymentGatewayException;
import br.com.carloslonghi.eletrolonghi.exception.ReferencedEntityNotFoundException;
import br.com.carloslonghi.eletrolonghi.repository.PaymentRepository;
import br.com.carloslonghi.eletrolonghi.support.TestFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
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
        Payment payment = TestFixtures.payment(1L);
        when(repairOrderService.findById(1L)).thenReturn(Optional.of(payment.getRepairOrder()));
        when(paymentRepository.existsByRepairOrderId(1L)).thenReturn(false);
        when(paymentRepository.save(payment)).thenReturn(payment);

        Payment saved = paymentService.save(payment);

        assertThat(saved.getStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(saved.getPaidAt()).isNull();
        verify(repairOrderService, never()).markPaymentReceived(any());
    }

    @Test
    void shouldThrowWhenRepairOrderMissingOnSave() {
        Payment payment = TestFixtures.payment(1L);
        when(repairOrderService.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.save(payment))
                .isInstanceOf(ReferencedEntityNotFoundException.class);
    }

    @Test
    void shouldThrowWhenRepairOrderAlreadyHasPayment() {
        Payment payment = TestFixtures.payment(1L);
        when(repairOrderService.findById(1L)).thenReturn(Optional.of(payment.getRepairOrder()));
        when(paymentRepository.existsByRepairOrderId(1L)).thenReturn(true);

        assertThatThrownBy(() -> paymentService.save(payment))
                .isInstanceOf(PaymentAlreadyExistsForRepairOrderException.class);
    }

    @Test
    void shouldNormalizeInstallmentsForNonCardMethod() {
        Payment payment = TestFixtures.payment(1L);
        payment.setMethod(PaymentMethod.PIX);
        payment.setInstallments(6);
        when(repairOrderService.findById(1L)).thenReturn(Optional.of(payment.getRepairOrder()));
        when(paymentRepository.existsByRepairOrderId(1L)).thenReturn(false);
        when(paymentRepository.save(payment)).thenReturn(payment);

        paymentService.save(payment);

        assertThat(payment.getInstallments()).isEqualTo(1);
    }

    @Test
    void shouldApproveOnSaveAndAdvanceRepairOrder() {
        Payment payment = TestFixtures.payment(1L);
        payment.setStatus(PaymentStatus.APPROVED);
        when(repairOrderService.findById(1L)).thenReturn(Optional.of(payment.getRepairOrder()));
        when(paymentRepository.existsByRepairOrderId(1L)).thenReturn(false);
        when(paymentRepository.save(payment)).thenReturn(payment);

        paymentService.save(payment);

        assertThat(payment.getPaidAt()).isNotNull();
        verify(repairOrderService).markPaymentReceived(1L);
    }

    @Test
    void shouldSetPaidAtAndAdvanceOrderWhenStatusBecomesApproved() {
        Payment payment = TestFixtures.payment(1L);
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(payment)).thenReturn(payment);

        Optional<Payment> updated = paymentService.updateStatus(1L, PaymentStatus.APPROVED);

        assertThat(updated).isPresent();
        assertThat(payment.getPaidAt()).isNotNull();
        verify(repairOrderService).markPaymentReceived(1L);
    }

    @Test
    void shouldNotReadvanceOrderWhenAlreadyPaid() {
        Payment payment = TestFixtures.payment(1L);
        payment.setStatus(PaymentStatus.APPROVED);
        payment.setPaidAt(java.time.LocalDateTime.now().minusDays(1));
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(payment)).thenReturn(payment);

        paymentService.updateStatus(1L, PaymentStatus.APPROVED);

        verify(repairOrderService, never()).markPaymentReceived(any());
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
    void shouldCreateCheckoutLinkAndPersistExternalReference() {
        Payment payment = checkoutPayment();
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(mercadoPagoClient.createCheckoutPreference(any(), any(), eq("payment-1")))
                .thenReturn(Optional.of(new CheckoutPreference("pref-1", "https://mp/checkout", "https://mp/sandbox")));

        Optional<CheckoutPreference> result = paymentService.createCheckoutLink(1L);

        assertThat(result).map(CheckoutPreference::initPoint).contains("https://mp/checkout");
        assertThat(payment.getExternalReference()).isEqualTo("payment-1");
        verify(paymentRepository).save(payment);
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
        when(mercadoPagoClient.createCheckoutPreference(any(), any(), any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.createCheckoutLink(1L))
                .isInstanceOf(PaymentGatewayException.class);
    }

    @Test
    void shouldReturnEmptyWhenCreatingCheckoutLinkForMissingPayment() {
        when(paymentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThat(paymentService.createCheckoutLink(1L)).isEmpty();
    }

    @Test
    void shouldSyncApprovedGatewayPaymentAndAdvanceOrder() {
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
        verify(repairOrderService).markPaymentReceived(1L);
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
        verify(repairOrderService, never()).markPaymentReceived(any());
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
        verify(repairOrderService, never()).markPaymentReceived(any());
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
        Payment payment = TestFixtures.payment(1L);
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        assertThat(paymentService.findById(1L)).contains(payment);
        paymentService.deleteById(1L);
        verify(paymentRepository).deleteById(1L);
    }
}
