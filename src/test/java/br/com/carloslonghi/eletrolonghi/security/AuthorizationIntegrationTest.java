package br.com.carloslonghi.eletrolonghi.security;

import br.com.carloslonghi.eletrolonghi.config.TokenService;
import br.com.carloslonghi.eletrolonghi.entity.Accessory;
import br.com.carloslonghi.eletrolonghi.entity.Brand;
import br.com.carloslonghi.eletrolonghi.entity.Customer;
import br.com.carloslonghi.eletrolonghi.entity.Device;
import br.com.carloslonghi.eletrolonghi.entity.RefreshToken;
import br.com.carloslonghi.eletrolonghi.entity.Payment;
import br.com.carloslonghi.eletrolonghi.entity.RepairOrder;
import br.com.carloslonghi.eletrolonghi.entity.User;
import br.com.carloslonghi.eletrolonghi.entity.enums.PaymentMethod;
import br.com.carloslonghi.eletrolonghi.entity.enums.PaymentStatus;
import br.com.carloslonghi.eletrolonghi.entity.enums.RepairOrderStatus;
import br.com.carloslonghi.eletrolonghi.entity.enums.Role;
import br.com.carloslonghi.eletrolonghi.repository.AccessoryRepository;
import br.com.carloslonghi.eletrolonghi.repository.BrandRepository;
import br.com.carloslonghi.eletrolonghi.repository.CustomerRepository;
import br.com.carloslonghi.eletrolonghi.repository.DeviceRepository;
import br.com.carloslonghi.eletrolonghi.repository.PaymentRepository;
import br.com.carloslonghi.eletrolonghi.repository.RefreshTokenRepository;
import br.com.carloslonghi.eletrolonghi.repository.RepairOrderRepository;
import br.com.carloslonghi.eletrolonghi.repository.UserRepository;
import br.com.carloslonghi.eletrolonghi.repository.support.AbstractPostgresIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

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

    @Test
    void shouldAllowUserToCreatePayment() throws Exception {
        RepairOrder repairOrder = createRepairOrder("SN-PAY-USER-001", "cliente.pay1@mail.com");

        mockMvc.perform(post("/payment")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":150.00,\"method\":\"CASH\",\"repairOrder\":" + repairOrder.getId() + "}"))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldForbidUserFromDeletingPayment() throws Exception {
        Payment payment = createPayment("SN-PAY-DEL-001", "cliente.pay2@mail.com");

        mockMvc.perform(delete("/payment/{id}", payment.getId())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAllowAdminToDeletePayment() throws Exception {
        Payment payment = createPayment("SN-PAY-DEL-002", "cliente.pay3@mail.com");

        mockMvc.perform(delete("/payment/{id}", payment.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldForbidUserFromListingUsers() throws Exception {
        mockMvc.perform(get("/user")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAllowAdminToListUsers() throws Exception {
        mockMvc.perform(get("/user")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    void shouldForbidUserFromUpdatingUserRole() throws Exception {
        User target = userRepository.save(User.builder()
                .name("Alvo Role").email("alvo.role@mail.com").password("senha").role(Role.USER).build());

        mockMvc.perform(patch("/user/{id}/role", target.getId())
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"ADMIN\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAllowAdminToUpdateUserRole() throws Exception {
        User target = userRepository.save(User.builder()
                .name("Alvo Role Admin").email("alvo.role.admin@mail.com").password("senha").role(Role.USER).build());

        mockMvc.perform(patch("/user/{id}/role", target.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"ADMIN\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldForbidUserFromUpdatingUserStatus() throws Exception {
        User target = userRepository.save(User.builder()
                .name("Alvo Status").email("alvo.status@mail.com").password("senha").role(Role.USER).build());

        mockMvc.perform(patch("/user/{id}/status", target.getId())
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"enabled\":false}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAllowAdminToSuspendUser() throws Exception {
        User target = userRepository.save(User.builder()
                .name("Alvo Suspenso").email("alvo.suspenso@mail.com").password("senha").role(Role.USER).enabled(true).build());

        mockMvc.perform(patch("/user/{id}/status", target.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"enabled\":false}"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldRejectLoginForDisabledUser() throws Exception {
        userRepository.save(User.builder()
                .name("Nao Ativado").email("nao.ativado@mail.com")
                .password(passwordEncoder.encode("senha123"))
                .role(Role.USER).enabled(false).build());

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"nao.ativado@mail.com\",\"password\":\"senha123\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRejectRefreshForDisabledUser() throws Exception {
        User disabledUser = userRepository.save(User.builder()
                .name("Suspenso Refresh").email("suspenso.refresh@mail.com")
                .password(passwordEncoder.encode("senha123"))
                .role(Role.USER).enabled(false).build());
        RefreshToken refreshToken = refreshTokenRepository.save(RefreshToken.builder()
                .token("valid-refresh-disabled-user")
                .user(disabledUser)
                .expiryDate(Instant.now().plusSeconds(3600))
                .revoked(false)
                .build());

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + refreshToken.getToken() + "\"}"))
                .andExpect(status().isUnauthorized());
    }

    private Payment createPayment(String serialNumber, String customerEmail) {
        RepairOrder repairOrder = createRepairOrder(serialNumber, customerEmail);
        return paymentRepository.save(Payment.builder()
                .amount(new BigDecimal("150.00"))
                .method(PaymentMethod.CASH)
                .status(PaymentStatus.PENDING)
                .installments(1)
                .repairOrder(repairOrder)
                .build());
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
