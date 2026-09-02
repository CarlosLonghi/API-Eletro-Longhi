package br.com.carloslonghi.eletrolonghi.service;

import br.com.carloslonghi.eletrolonghi.entity.Payment;
import br.com.carloslonghi.eletrolonghi.entity.RepairOrder;
import br.com.carloslonghi.eletrolonghi.entity.enums.PaymentMethod;
import br.com.carloslonghi.eletrolonghi.entity.enums.PaymentStatus;
import br.com.carloslonghi.eletrolonghi.exception.PaymentAlreadyExistsForRepairOrderException;
import br.com.carloslonghi.eletrolonghi.exception.ReferencedEntityNotFoundException;
import br.com.carloslonghi.eletrolonghi.repository.PaymentRepository;
import br.com.carloslonghi.eletrolonghi.repository.specification.PaymentSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final RepairOrderService repairOrderService;

    public Page<Payment> findAll(
            PaymentStatus status,
            PaymentMethod method,
            Long repairOrderId,
            LocalDateTime createdFrom,
            LocalDateTime createdTo,
            Pageable pageable
    ) {
        return paymentRepository.findAll(
                PaymentSpecification.withFilters(status, method, repairOrderId, createdFrom, createdTo),
                pageable
        );
    }

    @Transactional
    public Payment save(Payment payment) {
        RepairOrder repairOrder = resolveRepairOrder(payment.getRepairOrder());
        payment.setRepairOrder(repairOrder);

        if (paymentRepository.existsByRepairOrderId(repairOrder.getId())) {
            throw new PaymentAlreadyExistsForRepairOrderException(repairOrder.getId());
        }

        payment.setInstallments(normalizeInstallments(payment.getMethod(), payment.getInstallments()));

        if (payment.getStatus() == null) {
            payment.setStatus(PaymentStatus.PENDING);
        }

        if (payment.getStatus() == PaymentStatus.APPROVED) {
            applyApproved(payment);
        }

        return paymentRepository.save(payment);
    }

    public Optional<Payment> findById(Long id) {
        return paymentRepository.findById(id);
    }

    @Transactional
    public Optional<Payment> update(Long id, Payment payment) {
        return paymentRepository.findById(id).map(existing -> {
            existing.setAmount(payment.getAmount());
            existing.setMethod(payment.getMethod());
            existing.setInstallments(normalizeInstallments(payment.getMethod(), payment.getInstallments()));
            existing.setDescription(payment.getDescription());
            existing.setPayerName(payment.getPayerName());
            existing.setPayerDocument(payment.getPayerDocument());
            return paymentRepository.save(existing);
        });
    }

    @Transactional
    public Optional<Payment> updateStatus(Long id, PaymentStatus status) {
        return paymentRepository.findById(id).map(payment -> {
            payment.setStatus(status);
            if (status == PaymentStatus.APPROVED && payment.getPaidAt() == null) {
                applyApproved(payment);
            }
            return paymentRepository.save(payment);
        });
    }

    public void deleteById(Long id) {
        paymentRepository.deleteById(id);
    }

    private void applyApproved(Payment payment) {
        payment.setPaidAt(LocalDateTime.now());
        repairOrderService.markPaymentReceived(payment.getRepairOrder().getId());
    }

    private RepairOrder resolveRepairOrder(RepairOrder repairOrder) {
        Long id = repairOrder == null ? null : repairOrder.getId();
        return repairOrderService.findById(id)
                .orElseThrow(() -> new ReferencedEntityNotFoundException("RepairOrder", id));
    }

    private int normalizeInstallments(PaymentMethod method, Integer installments) {
        if (method != PaymentMethod.CARD || installments == null || installments < 1) {
            return 1;
        }
        return installments;
    }
}
