package br.com.carloslonghi.eletrolonghi.service;

import br.com.carloslonghi.eletrolonghi.entity.Customer;
import br.com.carloslonghi.eletrolonghi.entity.Device;
import br.com.carloslonghi.eletrolonghi.entity.Payment;
import br.com.carloslonghi.eletrolonghi.entity.RepairOrder;
import br.com.carloslonghi.eletrolonghi.entity.enums.PaymentStatus;
import br.com.carloslonghi.eletrolonghi.entity.enums.RepairOrderStatus;
import br.com.carloslonghi.eletrolonghi.entity.enums.Role;
import br.com.carloslonghi.eletrolonghi.exception.DeviceAlreadyInRepairException;
import br.com.carloslonghi.eletrolonghi.exception.EntityInUseException;
import br.com.carloslonghi.eletrolonghi.exception.InvalidRepairOrderStatusTransitionException;
import br.com.carloslonghi.eletrolonghi.exception.ReferencedEntityNotFoundException;
import br.com.carloslonghi.eletrolonghi.exception.RepairOrderNotPaidException;
import br.com.carloslonghi.eletrolonghi.exception.RepairOrderStatusActorNotAllowedException;
import br.com.carloslonghi.eletrolonghi.repository.PaymentRepository;
import br.com.carloslonghi.eletrolonghi.repository.RepairOrderRepository;
import br.com.carloslonghi.eletrolonghi.repository.specification.RepairOrderSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RepairOrderService {

    private final RepairOrderRepository repairOrderRepository;
    private final PaymentRepository paymentRepository;

    private final CustomerService customerService;
    private final DeviceService deviceService;

    public Page<RepairOrder> findAll(
            RepairOrderStatus status,
            PaymentStatus paymentStatus,
            Long customerId,
            Long deviceId,
            LocalDateTime createdFrom,
            LocalDateTime createdTo,
            Pageable pageable
    ) {
        return repairOrderRepository.findAll(
                RepairOrderSpecification.withFilters(
                        status,
                        paymentStatus,
                        customerId,
                        deviceId,
                        createdFrom,
                        createdTo
                ),
                pageable
        );
    }

    public RepairOrder save(RepairOrder repairOrder) {
        Customer customer = this.findCustomer(repairOrder.getCustomer());
        repairOrder.setCustomer(customer);

        Device device = this.findDevice(repairOrder.getDevice());
        repairOrder.setDevice(device);

        boolean hasActiveOrder = repairOrderRepository
                .existsByDeviceIdAndStatusNot(device.getId(), RepairOrderStatus.DEVICE_COLLECTED);

        if (hasActiveOrder) {
            throw new DeviceAlreadyInRepairException(device.getId());
        }

        return repairOrderRepository.save(repairOrder);
    }

    public Optional<RepairOrder> findById(Long id) {
        return repairOrderRepository.findById(id);
    }

    @Transactional
    public Optional<RepairOrder> update(Long id, RepairOrder repairOrder) {
        Optional<RepairOrder> optionalRepairOrder = repairOrderRepository.findById(id);

        if (optionalRepairOrder.isPresent()) {
            RepairOrder repairOrderToUpdate = optionalRepairOrder.get();

            Customer customer = this.findCustomer(repairOrder.getCustomer());
            Device device = this.findDevice(repairOrder.getDevice());

            // O status do serviço não é alterado por aqui: só o endpoint dedicado
            // PATCH /repair-order/{id}/status muda o status.
            repairOrderToUpdate.setDescription(repairOrder.getDescription());
            repairOrderToUpdate.setCustomer(customer);
            repairOrderToUpdate.setDevice(device);

            RepairOrder repairOrderUpdated = repairOrderRepository.save(repairOrderToUpdate);
            return Optional.of(repairOrderUpdated);
        }

        return Optional.empty();
    }

    @Transactional
    public Optional<RepairOrder> updateStatus(Long id, RepairOrderStatus status, Role actorRole) {
        return repairOrderRepository.findById(id).map(repairOrder -> {
            validateStatusTransition(repairOrder.getStatus(), status);
            guardActorAllowedForTransition(repairOrder, status, actorRole);
            guardDeviceCollected(repairOrder, status);
            repairOrder.setStatus(status);
            return repairOrderRepository.save(repairOrder);
        });
    }

    private void validateStatusTransition(RepairOrderStatus current, RepairOrderStatus next) {
        int distance = Math.abs(next.ordinal() - current.ordinal());

        if (distance != 1) {
            throw new InvalidRepairOrderStatusTransitionException(current, next);
        }
    }

    /**
     * TÉCNICO conduz o fluxo de oficina até {@code REPAIR_COMPLETED}, mas não marca a
     * retirada pelo cliente — isso é responsabilidade do atendimento (ATENDENTE) ou da
     * gestão. Por simetria, ATENDENTE só entra no fluxo de status para finalizar com
     * {@code DEVICE_COLLECTED}; as demais transições seguem exclusivas de TÉCNICO/gestão.
     */
    private void guardActorAllowedForTransition(RepairOrder order, RepairOrderStatus next, Role actorRole) {
        boolean isDeviceCollected = next == RepairOrderStatus.DEVICE_COLLECTED;

        if (actorRole == Role.TECNICO && isDeviceCollected) {
            throw new RepairOrderStatusActorNotAllowedException(order.getId(), actorRole, next);
        }
        if (actorRole == Role.ATENDENTE && !isDeviceCollected) {
            throw new RepairOrderStatusActorNotAllowedException(order.getId(), actorRole, next);
        }
    }

    /**
     * A ordem só pode ir para {@code DEVICE_COLLECTED} se tiver um pagamento
     * {@code APPROVED} vinculado — a garantia "não entrega aparelho não pago", agora
     * com fonte no {@code Payment} (o status da ordem não é mais tocado pelo pagamento).
     */
    private void guardDeviceCollected(RepairOrder order, RepairOrderStatus next) {
        if (next != RepairOrderStatus.DEVICE_COLLECTED) {
            return;
        }
        Payment payment = order.getPayment();
        if (payment == null || payment.getStatus() != PaymentStatus.APPROVED) {
            throw new RepairOrderNotPaidException(order.getId());
        }
    }

    public void deleteById(Long id) {
        if (paymentRepository.existsByRepairOrderId(id)) {
            throw new EntityInUseException("RepairOrder", id, "pagamento");
        }
        repairOrderRepository.deleteById(id);
    }

    private Customer findCustomer(Customer customer) {
        return customerService.findById(customer.getId())
                .orElseThrow(() -> new ReferencedEntityNotFoundException("Customer", customer.getId()));
    }

    private Device findDevice(Device device) {
        return deviceService.findById(device.getId())
                .orElseThrow(() -> new ReferencedEntityNotFoundException("Device", device.getId()));
    }
}
