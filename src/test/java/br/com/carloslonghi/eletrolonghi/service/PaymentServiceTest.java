package br.com.carloslonghi.eletrolonghi.service;

import br.com.carloslonghi.eletrolonghi.entity.Payment;
import br.com.carloslonghi.eletrolonghi.entity.enums.PaymentMethod;
import br.com.carloslonghi.eletrolonghi.entity.enums.PaymentStatus;
import br.com.carloslonghi.eletrolonghi.exception.PaymentAlreadyExistsForRepairOrderException;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private RepairOrderService repairOrderService;

    @InjectMocks
    private PaymentService paymentService;

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
    void shouldFindByIdAndDelete() {
        Payment payment = TestFixtures.payment(1L);
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        assertThat(paymentService.findById(1L)).contains(payment);
        paymentService.deleteById(1L);
        verify(paymentRepository).deleteById(1L);
    }
}
