package br.com.carloslonghi.eletrolonghi.service;

import br.com.carloslonghi.eletrolonghi.client.MercadoPagoClient;
import br.com.carloslonghi.eletrolonghi.client.dto.CheckoutPreference;
import br.com.carloslonghi.eletrolonghi.entity.Payment;
import br.com.carloslonghi.eletrolonghi.entity.RepairOrder;
import br.com.carloslonghi.eletrolonghi.entity.enums.PaymentMethod;
import br.com.carloslonghi.eletrolonghi.entity.enums.PaymentStatus;
import br.com.carloslonghi.eletrolonghi.exception.InvalidPaymentCheckoutException;
import br.com.carloslonghi.eletrolonghi.exception.PaymentAlreadyExistsForRepairOrderException;
import br.com.carloslonghi.eletrolonghi.exception.PaymentGatewayException;
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
    private final MercadoPagoClient mercadoPagoClient;

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
            applyStatus(payment, status);
            return paymentRepository.save(payment);
        });
    }

    /**
     * Gera o link do Checkout Pro para um pagamento pendente com forma
     * {@code MERCADO_PAGO_CHECKOUT} e grava o {@code externalReference} usado no polling.
     */
    @Transactional
    public Optional<CheckoutPreference> createCheckoutLink(Long id) {
        return paymentRepository.findById(id).map(payment -> {
            if (payment.getMethod() != PaymentMethod.MERCADO_PAGO_CHECKOUT) {
                throw new InvalidPaymentCheckoutException(
                        "O link de pagamento só está disponível para pagamentos com forma MERCADO_PAGO_CHECKOUT.");
            }
            if (payment.getStatus() != PaymentStatus.PENDING) {
                throw new InvalidPaymentCheckoutException(
                        "O link de pagamento só pode ser gerado para um pagamento pendente.");
            }

            String externalReference = externalReferenceFor(payment);
            CheckoutPreference preference = mercadoPagoClient
                    .createCheckoutPreference(checkoutTitle(payment), payment.getAmount(), externalReference)
                    .orElseThrow(() -> new PaymentGatewayException(
                            "Não foi possível gerar o link de pagamento no Mercado Pago."));

            payment.setExternalReference(externalReference);
            paymentRepository.save(payment);

            return preference;
        });
    }

    /**
     * Concilia a situação do pagamento com o Mercado Pago por polling: busca o pagamento
     * real pelo {@code externalReference} e reaplica o status. Sem alteração quando ainda
     * não há pagamento no gateway.
     */
    @Transactional
    public Optional<Payment> syncWithGateway(Long id) {
        return paymentRepository.findById(id).map(payment -> {
            if (payment.getExternalReference() == null) {
                throw new InvalidPaymentCheckoutException(
                        "O pagamento não possui link do Checkout Pro para conciliar.");
            }

            return mercadoPagoClient.findPaymentByExternalReference(payment.getExternalReference())
                    .map(snapshot -> {
                        if (snapshot.id() != null) {
                            payment.setGatewayPaymentId(String.valueOf(snapshot.id()));
                        }
                        PaymentStatus mapped = mapGatewayStatus(snapshot.status());
                        if (mapped != null && mapped != payment.getStatus()) {
                            applyStatus(payment, mapped);
                        }
                        return paymentRepository.save(payment);
                    })
                    .orElse(payment);
        });
    }

    public void deleteById(Long id) {
        paymentRepository.deleteById(id);
    }

    private void applyStatus(Payment payment, PaymentStatus status) {
        payment.setStatus(status);
        if (status == PaymentStatus.APPROVED && payment.getPaidAt() == null) {
            applyApproved(payment);
        }
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

    private static String externalReferenceFor(Payment payment) {
        return "payment-" + payment.getId();
    }

    private static String checkoutTitle(Payment payment) {
        RepairOrder order = payment.getRepairOrder();
        String base = "Reparo #" + order.getId();
        if (order.getDescription() != null && !order.getDescription().isBlank()) {
            base = base + " - " + order.getDescription();
        }
        return base.length() > 250 ? base.substring(0, 250) : base;
    }

    private static PaymentStatus mapGatewayStatus(String gatewayStatus) {
        if (gatewayStatus == null) {
            return null;
        }
        return switch (gatewayStatus) {
            case "approved", "authorized" -> PaymentStatus.APPROVED;
            case "pending", "in_process", "in_mediation" -> PaymentStatus.PENDING;
            case "rejected" -> PaymentStatus.REJECTED;
            case "cancelled" -> PaymentStatus.CANCELLED;
            case "refunded", "charged_back" -> PaymentStatus.REFUNDED;
            default -> null;
        };
    }
}
