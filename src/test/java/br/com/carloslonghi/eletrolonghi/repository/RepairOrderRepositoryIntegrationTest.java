package br.com.carloslonghi.eletrolonghi.repository;

import br.com.carloslonghi.eletrolonghi.entity.Accessory;
import br.com.carloslonghi.eletrolonghi.entity.Brand;
import br.com.carloslonghi.eletrolonghi.entity.Customer;
import br.com.carloslonghi.eletrolonghi.entity.Device;
import br.com.carloslonghi.eletrolonghi.entity.RepairOrder;
import br.com.carloslonghi.eletrolonghi.entity.enums.RepairOrderStatus;
import br.com.carloslonghi.eletrolonghi.repository.specification.RepairOrderSpecification;
import br.com.carloslonghi.eletrolonghi.repository.support.AbstractPostgresIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class RepairOrderRepositoryIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private RepairOrderRepository repairOrderRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private AccessoryRepository accessoryRepository;

    @BeforeEach
    void clean() {
        repairOrderRepository.deleteAll();
        deviceRepository.deleteAll();
        customerRepository.deleteAll();
        accessoryRepository.deleteAll();
        brandRepository.deleteAll();
    }

    @Test
    void shouldCheckActiveOrderAndApplyFilters() {
        Brand brand = brandRepository.save(Brand.builder().name("LG-TEST").build());
        Accessory accessory = accessoryRepository.save(Accessory.builder().name("Controle").build());
        Device device = deviceRepository.save(Device.builder()
                .model("TV")
                .serialNumber("SER-100")
                .brand(brand)
                .accessories(List.of(accessory))
                .build());
        Customer customer = customerRepository.save(Customer.builder().name("Cliente").phone("11999").email("cliente@mail.com").build());

        repairOrderRepository.save(RepairOrder.builder()
                .description("Troca de fonte")
                .status(RepairOrderStatus.IN_REPAIR)
                .customer(customer)
                .device(device)
                .build());

        assertThat(repairOrderRepository.existsByDeviceIdAndStatusNot(device.getId(), RepairOrderStatus.DEVICE_COLLECTED)).isTrue();
        assertThat(repairOrderRepository.findAll(RepairOrderSpecification.withFilters(
                RepairOrderStatus.IN_REPAIR,
                customer.getId(),
                device.getId(),
                null,
                null
        ))).hasSize(1);
    }
}

