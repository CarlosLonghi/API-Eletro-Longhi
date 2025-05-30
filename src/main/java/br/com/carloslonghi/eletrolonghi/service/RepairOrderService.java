package br.com.carloslonghi.eletrolonghi.service;

import br.com.carloslonghi.eletrolonghi.entity.Customer;
import br.com.carloslonghi.eletrolonghi.entity.Device;
import br.com.carloslonghi.eletrolonghi.entity.RepairOrder;
import br.com.carloslonghi.eletrolonghi.repository.RepairOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

    public RepairOrder save(RepairOrder repairOrder) {
        Customer customer = this.findCustomer(repairOrder.getCustomer());
        repairOrder.setCustomer(customer);

        Device device = this.findDevice(repairOrder.getDevice());
        repairOrder.setDevice(device);

        return repairOrderRepository.save(repairOrder);
    }

    public Optional<RepairOrder> findById(Long id) {
        return repairOrderRepository.findById(id);
    }

    public Optional<RepairOrder> update(Long id, RepairOrder repairOrder) {
        Optional<RepairOrder> optionalRepairOrder = repairOrderRepository.findById(id);

        if (optionalRepairOrder.isPresent()) {
            Customer customer = this.findCustomer(repairOrder.getCustomer());
            Device device = this.findDevice(repairOrder.getDevice());

            RepairOrder repairOrderToUpdate = optionalRepairOrder.get();
            repairOrderToUpdate.setDescription(repairOrder.getDescription());
            repairOrderToUpdate.setStatus(repairOrder.getStatus());
            repairOrderToUpdate.setCustomer(customer);
            repairOrderToUpdate.setDevice(device);

            RepairOrder repairOrderUpdated = repairOrderRepository.save(repairOrderToUpdate);
            return Optional.of(repairOrderUpdated);
        }

        return Optional.empty();
    }

    public void deleteById(Long id) {
        repairOrderRepository.deleteById(id);
    }

    private Customer findCustomer(Customer customer) {
        return customerService.findById(customer.getId()).orElse(null);
    }

    private Device findDevice(Device device) {
        return deviceService.findById(device.getId()).orElse(null);
    }
}
