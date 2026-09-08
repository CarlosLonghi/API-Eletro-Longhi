package br.com.carloslonghi.eletrolonghi.repository;

import br.com.carloslonghi.eletrolonghi.entity.Accessory;
import br.com.carloslonghi.eletrolonghi.entity.Brand;
import br.com.carloslonghi.eletrolonghi.entity.Customer;
import br.com.carloslonghi.eletrolonghi.entity.Device;
import br.com.carloslonghi.eletrolonghi.entity.Payment;
import br.com.carloslonghi.eletrolonghi.entity.RepairOrder;
import br.com.carloslonghi.eletrolonghi.entity.enums.PaymentMethod;
import br.com.carloslonghi.eletrolonghi.entity.enums.PaymentStatus;
import br.com.carloslonghi.eletrolonghi.entity.enums.RepairOrderStatus;
import br.com.carloslonghi.eletrolonghi.repository.specification.RepairOrderSpecification;
import br.com.carloslonghi.eletrolonghi.repository.support.AbstractPostgresIntegrationTest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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

    @Autowired
    private PaymentRepository paymentRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @BeforeEach
    void clean() {
        paymentRepository.deleteAll();
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
                null,
                customer.getId(),
                device.getId(),
                null,
                null
        ))).hasSize(1);
    }

    @Test
    void shouldFilterByPaymentStatusAndEagerLoadPaymentOnListing() {
        Brand brand = brandRepository.save(Brand.builder().name("SONY-TEST").build());
        Device device = deviceRepository.save(Device.builder()
                .model("Soundbar").serialNumber("SER-200").brand(brand).accessories(List.of()).build());
        Customer customer = customerRepository.save(Customer.builder()
                .name("Cliente 2").phone("11988").email("cliente2@mail.com").build());
        RepairOrder order = repairOrderRepository.save(RepairOrder.builder()
                .description("Troca de alto-falante")
                .status(RepairOrderStatus.REPAIR_COMPLETED)
                .customer(customer)
                .device(device)
                .build());
        paymentRepository.save(Payment.builder()
                .amount(new BigDecimal("250.00"))
                .method(PaymentMethod.PIX)
                .status(PaymentStatus.APPROVED)
                .installments(1)
                .repairOrder(order)
                .build());

        entityManager.flush();
        entityManager.clear();

        var approved = repairOrderRepository.findAll(RepairOrderSpecification.withFilters(
                null, PaymentStatus.APPROVED, null, null, null, null), PageRequest.of(0, 10));
        assertThat(approved.getContent()).hasSize(1);
        assertThat(approved.getContent().getFirst().getPayment().getStatus()).isEqualTo(PaymentStatus.APPROVED);

        assertThat(repairOrderRepository.findAll(RepairOrderSpecification.withFilters(
                null, PaymentStatus.PENDING, null, null, null, null), PageRequest.of(0, 10)).getContent()).isEmpty();
    }
}

