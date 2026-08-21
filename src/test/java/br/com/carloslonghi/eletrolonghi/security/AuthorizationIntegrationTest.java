package br.com.carloslonghi.eletrolonghi.security;

import br.com.carloslonghi.eletrolonghi.config.TokenService;
import br.com.carloslonghi.eletrolonghi.entity.Accessory;
import br.com.carloslonghi.eletrolonghi.entity.Brand;
import br.com.carloslonghi.eletrolonghi.entity.Customer;
import br.com.carloslonghi.eletrolonghi.entity.Device;
import br.com.carloslonghi.eletrolonghi.entity.RepairOrder;
import br.com.carloslonghi.eletrolonghi.entity.User;
import br.com.carloslonghi.eletrolonghi.entity.enums.RepairOrderStatus;
import br.com.carloslonghi.eletrolonghi.entity.enums.Role;
import br.com.carloslonghi.eletrolonghi.repository.AccessoryRepository;
import br.com.carloslonghi.eletrolonghi.repository.BrandRepository;
import br.com.carloslonghi.eletrolonghi.repository.CustomerRepository;
import br.com.carloslonghi.eletrolonghi.repository.DeviceRepository;
import br.com.carloslonghi.eletrolonghi.repository.RepairOrderRepository;
import br.com.carloslonghi.eletrolonghi.repository.support.AbstractPostgresIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
class AuthorizationIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private AccessoryRepository accessoryRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private RepairOrderRepository repairOrderRepository;

    private String adminToken;
    private String userToken;

    @BeforeEach
    void setUpTokens() {
        User admin = User.builder().id(1L).name("Admin").email("admin@mail.com").role(Role.ADMIN).build();
        User user = User.builder().id(2L).name("User").email("user@mail.com").role(Role.USER).build();
        adminToken = tokenService.generateToken(admin);
        userToken = tokenService.generateToken(user);
    }

    @Test
    void shouldRejectRequestWithoutTokenAsUnauthenticated() throws Exception {
        mockMvc.perform(post("/brand")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Marca Teste Brand\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldForbidUserFromCreatingBrand() throws Exception {
        mockMvc.perform(post("/brand")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Marca Teste Brand\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAllowAdminToCreateBrand() throws Exception {
        mockMvc.perform(post("/brand")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Marca Teste Brand\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldForbidUserFromDeletingBrand() throws Exception {
        Brand brand = brandRepository.save(Brand.builder().name("LG Teste").build());

        mockMvc.perform(delete("/brand/{id}", brand.getId())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAllowAdminToDeleteBrand() throws Exception {
        Brand brand = brandRepository.save(Brand.builder().name("Motorola").build());

        mockMvc.perform(delete("/brand/{id}", brand.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldForbidUserFromCreatingAccessory() throws Exception {
        mockMvc.perform(post("/accessory")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Cabo USB\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAllowAdminToCreateAccessory() throws Exception {
        mockMvc.perform(post("/accessory")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Cabo USB\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldForbidUserFromDeletingAccessory() throws Exception {
        Accessory accessory = accessoryRepository.save(Accessory.builder().name("Fone de ouvido").build());

        mockMvc.perform(delete("/accessory/{id}", accessory.getId())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAllowAdminToDeleteAccessory() throws Exception {
        Accessory accessory = accessoryRepository.save(Accessory.builder().name("Carregador").build());

        mockMvc.perform(delete("/accessory/{id}", accessory.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldAllowUserToCreateCustomer() throws Exception {
        mockMvc.perform(post("/customer")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Cliente Teste\",\"phone\":\"11999990000\",\"email\":\"cliente.teste@mail.com\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldForbidUserFromDeletingCustomer() throws Exception {
        Customer customer = customerRepository.save(Customer.builder()
                .name("Cliente Um").phone("11999990001").email("cliente.um@mail.com").build());

        mockMvc.perform(delete("/customer/{id}", customer.getId())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAllowAdminToDeleteCustomer() throws Exception {
        Customer customer = customerRepository.save(Customer.builder()
                .name("Cliente Dois").phone("11999990002").email("cliente.dois@mail.com").build());

        mockMvc.perform(delete("/customer/{id}", customer.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldAllowUserToCreateDevice() throws Exception {
        Brand brand = brandRepository.save(Brand.builder().name("Apple").build());

        mockMvc.perform(post("/device")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"model\":\"iPhone 15\",\"serialNumber\":\"SN-USER-001\",\"brand\":"
                                + brand.getId() + ",\"accessories\":[]}"))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldForbidUserFromDeletingDevice() throws Exception {
        Brand brand = brandRepository.save(Brand.builder().name("Xiaomi").build());
        Device device = deviceRepository.save(Device.builder()
                .model("Redmi Note").serialNumber("SN-DEL-001").brand(brand).accessories(List.of()).build());

        mockMvc.perform(delete("/device/{id}", device.getId())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAllowAdminToDeleteDevice() throws Exception {
        Brand brand = brandRepository.save(Brand.builder().name("Sony").build());
        Device device = deviceRepository.save(Device.builder()
                .model("Xperia").serialNumber("SN-DEL-002").brand(brand).accessories(List.of()).build());

        mockMvc.perform(delete("/device/{id}", device.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldAllowUserToCreateRepairOrder() throws Exception {
        Brand brand = brandRepository.save(Brand.builder().name("Nokia").build());
        Device device = deviceRepository.save(Device.builder()
                .model("Nokia 3310").serialNumber("SN-RO-USER-001").brand(brand).accessories(List.of()).build());
        Customer customer = customerRepository.save(Customer.builder()
                .name("Cliente Tres").phone("11999990003").email("cliente.tres@mail.com").build());

        mockMvc.perform(post("/repair-order")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"Tela quebrada\",\"status\":\"AWAITING_EVALUATION\",\"customer\":"
                                + customer.getId() + ",\"device\":" + device.getId() + "}"))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldForbidUserFromDeletingRepairOrder() throws Exception {
        RepairOrder repairOrder = createRepairOrder("SN-RO-DEL-001", "cliente.quatro@mail.com");

        mockMvc.perform(delete("/repair-order/{id}", repairOrder.getId())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAllowAdminToDeleteRepairOrder() throws Exception {
        RepairOrder repairOrder = createRepairOrder("SN-RO-DEL-002", "cliente.cinco@mail.com");

        mockMvc.perform(delete("/repair-order/{id}", repairOrder.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }

    private RepairOrder createRepairOrder(String serialNumber, String customerEmail) {
        Brand brand = brandRepository.save(Brand.builder().name("Brand-" + serialNumber).build());
        Device device = deviceRepository.save(Device.builder()
                .model("Modelo").serialNumber(serialNumber).brand(brand).accessories(List.of()).build());
        Customer customer = customerRepository.save(Customer.builder()
                .name("Cliente").phone("11999990009").email(customerEmail).build());
        return repairOrderRepository.save(RepairOrder.builder()
                .description("Reparo")
                .status(RepairOrderStatus.AWAITING_EVALUATION)
                .customer(customer)
                .device(device)
                .build());
    }
}
