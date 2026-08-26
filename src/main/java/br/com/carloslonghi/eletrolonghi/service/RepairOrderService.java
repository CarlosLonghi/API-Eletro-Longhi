package br.com.carloslonghi.eletrolonghi.service;

import br.com.carloslonghi.eletrolonghi.entity.Customer;
import br.com.carloslonghi.eletrolonghi.entity.Device;
import br.com.carloslonghi.eletrolonghi.entity.RepairOrder;
import br.com.carloslonghi.eletrolonghi.entity.enums.RepairOrderStatus;
import br.com.carloslonghi.eletrolonghi.exception.DeviceAlreadyInRepairException;
import br.com.carloslonghi.eletrolonghi.exception.InvalidRepairOrderStatusTransitionException;
import br.com.carloslonghi.eletrolonghi.exception.ReferencedEntityNotFoundException;
import br.com.carloslonghi.eletrolonghi.repository.RepairOrderRepository;
import br.com.carloslonghi.eletrolonghi.repository.specification.RepairOrderSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RepairOrderService {

    private final RepairOrderRepository repairOrderRepository;

    private final CustomerService customerService;
    private final DeviceService deviceService;

    public List<RepairOrder> findAll() {
        return repairOrderRepository.findAll();
    }

    public Page<RepairOrder> findAll(
            RepairOrderStatus status,
            Long customerId,
            Long deviceId,
            LocalDateTime createdFrom,
            LocalDateTime createdTo,
            Pageable pageable
    ) {
        return repairOrderRepository.findAll(
                RepairOrderSpecification.withFilters(
                        status,
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

    public Optional<RepairOrder> update(Long id, RepairOrder repairOrder) {
        Optional<RepairOrder> optionalRepairOrder = repairOrderRepository.findById(id);

        if (optionalRepairOrder.isPresent()) {
            RepairOrder repairOrderToUpdate = optionalRepairOrder.get();
            validateStatusTransition(repairOrderToUpdate.getStatus(), repairOrder.getStatus());

            Customer customer = this.findCustomer(repairOrder.getCustomer());
            Device device = this.findDevice(repairOrder.getDevice());

            repairOrderToUpdate.setDescription(repairOrder.getDescription());
            repairOrderToUpdate.setStatus(repairOrder.getStatus());
            repairOrderToUpdate.setCustomer(customer);
            repairOrderToUpdate.setDevice(device);

            RepairOrder repairOrderUpdated = repairOrderRepository.save(repairOrderToUpdate);
            return Optional.of(repairOrderUpdated);
        }

        return Optional.empty();
    }

    public Optional<RepairOrder> updateStatus(Long id, RepairOrderStatus status) {
        return repairOrderRepository.findById(id).map(repairOrder -> {
            validateStatusTransition(repairOrder.getStatus(), status);
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

    public void deleteById(Long id) {
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
