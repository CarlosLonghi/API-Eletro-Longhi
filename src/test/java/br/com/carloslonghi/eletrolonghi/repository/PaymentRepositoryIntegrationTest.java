package br.com.carloslonghi.eletrolonghi.repository;

import br.com.carloslonghi.eletrolonghi.entity.Brand;
import br.com.carloslonghi.eletrolonghi.entity.Customer;
import br.com.carloslonghi.eletrolonghi.entity.Device;
import br.com.carloslonghi.eletrolonghi.entity.Payment;
import br.com.carloslonghi.eletrolonghi.entity.RepairOrder;
import br.com.carloslonghi.eletrolonghi.entity.enums.PaymentMethod;
import br.com.carloslonghi.eletrolonghi.entity.enums.PaymentStatus;
import br.com.carloslonghi.eletrolonghi.entity.enums.RepairOrderStatus;
import br.com.carloslonghi.eletrolonghi.repository.specification.PaymentSpecification;
import br.com.carloslonghi.eletrolonghi.repository.support.AbstractPostgresIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class PaymentRepositoryIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private RepairOrderRepository repairOrderRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private BrandRepository brandRepository;

    @BeforeEach
    void clean() {
        paymentRepository.deleteAll();
        repairOrderRepository.deleteAll();
        deviceRepository.deleteAll();
        customerRepository.deleteAll();
        brandRepository.deleteAll();
    }

    @Test
    void shouldPersistAndQueryPayments() {
        RepairOrder order = persistRepairOrder("SER-PAY-1", "pay1@mail.com");

        Payment payment = paymentRepository.save(Payment.builder()
                .amount(new BigDecimal("199.90"))
                .method(PaymentMethod.PIX)
                .status(PaymentStatus.APPROVED)
                .installments(1)
                .repairOrder(order)
                .build());

        assertThat(paymentRepository.findByRepairOrderId(order.getId())).contains(payment);
        assertThat(paymentRepository.existsByRepairOrderId(order.getId())).isTrue();
        assertThat(paymentRepository.findAll(PaymentSpecification.withFilters(
                PaymentStatus.APPROVED, PaymentMethod.PIX, order.getId(), null, null))).hasSize(1);
        assertThat(paymentRepository.findAll(PaymentSpecification.withFilters(
                PaymentStatus.PENDING, null, null, null, null))).isEmpty();
    }

    private RepairOrder persistRepairOrder(String serialNumber, String customerEmail) {
        Brand brand = brandRepository.save(Brand.builder().name("Brand-" + serialNumber).build());
        Device device = deviceRepository.save(Device.builder()
                .model("Modelo").serialNumber(serialNumber).brand(brand).accessories(List.of()).build());
        Customer customer = customerRepository.save(Customer.builder()
                .name("Cliente").phone("11999990000").email(customerEmail).build());
        return repairOrderRepository.save(RepairOrder.builder()
                .description("Reparo")
                .status(RepairOrderStatus.REPAIR_COMPLETED)
                .customer(customer)
                .device(device)
                .build());
    }
}
