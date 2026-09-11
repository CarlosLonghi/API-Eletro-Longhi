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
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Cobre a matriz de autorização por papel (ADMIN / GERENTE / ATENDENTE / TECNICO / PENDENTE)
 * definida em {@code config/SecurityConfig}, além do comportamento de soft delete: todo
 * {@code DELETE} da API marca a linha (não remove) e a gestão de dependências ativas retorna 409.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
class AuthorizationIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityManager entityManager;

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
    private String gerenteToken;
    private String atendenteToken;
    private String tecnicoToken;
    private String pendenteToken;

    @BeforeEach
    void setUpTokens() {
        adminToken = tokenFor(1L, Role.ADMIN);
        gerenteToken = tokenFor(2L, Role.GERENTE);
        atendenteToken = tokenFor(3L, Role.ATENDENTE);
        tecnicoToken = tokenFor(4L, Role.TECNICO);
        pendenteToken = tokenFor(5L, Role.PENDENTE);
    }

    private String tokenFor(Long id, Role role) {
        return tokenService.generateToken(User.builder()
                .id(id).name(role.name()).email(role.name().toLowerCase() + "@mail.com").role(role).build());
    }

    private long countSoftDeleted(String table, Long id) {
        entityManager.flush();
        Long count = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM " + table + " WHERE id = ? AND deleted_at IS NOT NULL", Long.class, id);
        return count == null ? 0 : count;
    }

    // --- Sem token -----------------------------------------------------------

    @Test
    void shouldRejectRequestWithoutTokenAsUnauthenticated() throws Exception {
        mockMvc.perform(get("/brand"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldForbidPendenteFromEverything() throws Exception {
        mockMvc.perform(get("/brand").header("Authorization", "Bearer " + pendenteToken))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/repair-order").header("Authorization", "Bearer " + pendenteToken))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/customer").header("Authorization", "Bearer " + pendenteToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"X\",\"phone\":\"1\",\"email\":\"x@mail.com\"}"))
                .andExpect(status().isForbidden());
    }

    // --- Brand / Accessory --------------------------------------------------

    @Test
    void atendenteReadsBrandsButCannotCreate() throws Exception {
        mockMvc.perform(get("/brand").header("Authorization", "Bearer " + atendenteToken))
                .andExpect(status().isOk());
        mockMvc.perform(post("/brand").header("Authorization", "Bearer " + atendenteToken)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Marca A\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void tecnicoHasNoAccessToBrands() throws Exception {
        mockMvc.perform(get("/brand").header("Authorization", "Bearer " + tecnicoToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void gerenteCreatesAndSoftDeletesBrand() throws Exception {
        mockMvc.perform(post("/brand").header("Authorization", "Bearer " + gerenteToken)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Marca Gerente\"}"))
                .andExpect(status().isCreated());

        Brand brand = brandRepository.save(Brand.builder().name("Para apagar").build());
        mockMvc.perform(delete("/brand/{id}", brand.getId()).header("Authorization", "Bearer " + gerenteToken))
                .andExpect(status().isNoContent());

        assertThat(brandRepository.findById(brand.getId())).isEmpty();
        assertThat(countSoftDeleted("brands", brand.getId())).isEqualTo(1);
    }

    @Test
    void shouldRejectDeletingBrandThatStillHasDevices() throws Exception {
        Brand brand = brandRepository.save(Brand.builder().name("Com aparelho").build());
        deviceRepository.save(Device.builder()
                .model("M").serialNumber("SN-INUSE-1").brand(brand).accessories(List.of()).build());

        mockMvc.perform(delete("/brand/{id}", brand.getId()).header("Authorization", "Bearer " + gerenteToken))
                .andExpect(status().isConflict());
    }

    @Test
    void atendenteCannotDeleteAccessoryGerenteCan() throws Exception {
        Accessory accessory = accessoryRepository.save(Accessory.builder().name("Cabo").build());

        mockMvc.perform(delete("/accessory/{id}", accessory.getId()).header("Authorization", "Bearer " + atendenteToken))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/accessory/{id}", accessory.getId()).header("Authorization", "Bearer " + gerenteToken))
                .andExpect(status().isNoContent());
    }

    // --- Customer / Device -------------------------------------------------

    @Test
    void atendenteManagesCustomersButCannotDelete() throws Exception {
        mockMvc.perform(post("/customer").header("Authorization", "Bearer " + atendenteToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Cliente\",\"phone\":\"11999990000\",\"email\":\"c@mail.com\"}"))
                .andExpect(status().isCreated());

        Customer customer = customerRepository.save(Customer.builder()
                .name("Del").phone("1").email("del@mail.com").build());
        mockMvc.perform(delete("/customer/{id}", customer.getId()).header("Authorization", "Bearer " + atendenteToken))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/customer/{id}", customer.getId()).header("Authorization", "Bearer " + gerenteToken))
                .andExpect(status().isNoContent());
        assertThat(countSoftDeleted("customers", customer.getId())).isEqualTo(1);
    }

    @Test
    void tecnicoReadsDeviceDetailButNotListOrWrites() throws Exception {
        Device device = device("SN-T-1");

        // detalhe do aparelho: liberado (o técnico chega nele pela ordem de reparo)
        mockMvc.perform(get("/device/{id}", device.getId()).header("Authorization", "Bearer " + tecnicoToken))
                .andExpect(status().isOk());

        // listagem, busca por série e escrita seguem fora do alcance do técnico
        mockMvc.perform(get("/device").header("Authorization", "Bearer " + tecnicoToken))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/device/serial-number?serialNumber=SN-T-1").header("Authorization", "Bearer " + tecnicoToken))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/device").header("Authorization", "Bearer " + tecnicoToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"model\":\"M\",\"serialNumber\":\"SN-T-2\",\"brand\":1,\"accessories\":[]}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void atendenteCreatesDevice() throws Exception {
        Brand brand = brandRepository.save(Brand.builder().name("Apple").build());
        mockMvc.perform(post("/device").header("Authorization", "Bearer " + atendenteToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"model\":\"iPhone\",\"serialNumber\":\"SN-A-1\",\"brand\":" + brand.getId() + ",\"accessories\":[]}"))
                .andExpect(status().isCreated());
    }

    // --- RepairOrder ------------------------------------------------------

    @Test
    void atendenteOpensRepairOrderTecnicoCannot() throws Exception {
        Device device = device("SN-RO-1");
        Customer customer = customerRepository.save(Customer.builder().name("C").phone("1").email("ro1@mail.com").build());
        String body = "{\"description\":\"d\",\"status\":\"AWAITING_EVALUATION\",\"customer\":"
                + customer.getId() + ",\"device\":" + device.getId() + "}";

        mockMvc.perform(post("/repair-order").header("Authorization", "Bearer " + tecnicoToken)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/repair-order").header("Authorization", "Bearer " + atendenteToken)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());
    }

    @Test
    void tecnicoReadsRepairOrders() throws Exception {
        mockMvc.perform(get("/repair-order").header("Authorization", "Bearer " + tecnicoToken))
                .andExpect(status().isOk());
    }

    @Test
    void listsRepairOrderThatHasNoPayment() throws Exception {
        RepairOrder order = createRepairOrder("SN-NOPAY-1", "nopay@mail.com");

        // regressão: Payment com @SoftDelete quebrava o @OneToOne inverso de toda
        // ordem sem pagamento (FetchNotFoundException -> 500).
        mockMvc.perform(get("/repair-order?page=0&size=10&sortBy=id&direction=asc")
                        .header("Authorization", "Bearer " + atendenteToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].paymentStatus").value(org.hamcrest.Matchers.nullValue()))
                .andExpect(jsonPath("$.content[0].paymentId").value(org.hamcrest.Matchers.nullValue()));

        mockMvc.perform(get("/repair-order/{id}", order.getId()).header("Authorization", "Bearer " + atendenteToken))
                .andExpect(status().isOk());
    }

    @Test
    void tecnicoAndManagementDriveStatusButNotToDeviceCollected() throws Exception {
        RepairOrder order = createRepairOrder("SN-ST-1", "st1@mail.com");
        String body = "{\"status\":\"IN_EVALUATION\"}";

        // ATENDENTE só entra no fluxo de status para finalizar com DEVICE_COLLECTED.
        mockMvc.perform(patch("/repair-order/{id}/status", order.getId())
                        .header("Authorization", "Bearer " + atendenteToken)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());
        mockMvc.perform(patch("/repair-order/{id}/status", order.getId())
                        .header("Authorization", "Bearer " + tecnicoToken)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk());
        mockMvc.perform(patch("/repair-order/{id}/status", order.getId())
                        .header("Authorization", "Bearer " + gerenteToken)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"AWAITING_APPROVAL\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void tecnicoCannotMarkDeviceCollected() throws Exception {
        RepairOrder order = createRepairOrderReadyForCollection("SN-DC-TEC", "dctec@mail.com");

        mockMvc.perform(patch("/repair-order/{id}/status", order.getId())
                        .header("Authorization", "Bearer " + tecnicoToken)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"DEVICE_COLLECTED\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void atendenteCanMarkDeviceCollectedWhenPaid() throws Exception {
        RepairOrder order = createRepairOrderReadyForCollection("SN-DC-ATE", "dcate@mail.com");

        mockMvc.perform(patch("/repair-order/{id}/status", order.getId())
                        .header("Authorization", "Bearer " + atendenteToken)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"DEVICE_COLLECTED\"}"))
                .andExpect(status().isOk());

        assertThat(repairOrderRepository.findById(order.getId()))
                .get()
                .extracting(RepairOrder::getStatus)
                .isEqualTo(RepairOrderStatus.DEVICE_COLLECTED);
    }

    @Test
    void gerenteOrAdminCanStillMarkDeviceCollectedWhenPaid() throws Exception {
        RepairOrder gerenteOrder = createRepairOrderReadyForCollection("SN-DC-GER", "dcger@mail.com");
        mockMvc.perform(patch("/repair-order/{id}/status", gerenteOrder.getId())
                        .header("Authorization", "Bearer " + gerenteToken)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"DEVICE_COLLECTED\"}"))
                .andExpect(status().isOk());

        RepairOrder adminOrder = createRepairOrderReadyForCollection("SN-DC-ADM", "dcadm@mail.com");
        mockMvc.perform(patch("/repair-order/{id}/status", adminOrder.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"DEVICE_COLLECTED\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void pendenteStillForbiddenOnStatusPatch() throws Exception {
        RepairOrder order = createRepairOrder("SN-ST-PEND", "stpend@mail.com");

        mockMvc.perform(patch("/repair-order/{id}/status", order.getId())
                        .header("Authorization", "Bearer " + pendenteToken)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"IN_EVALUATION\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void repairOrderPutDoesNotChangeStatus() throws Exception {
        RepairOrder order = createRepairOrder("SN-PUT-1", "put1@mail.com");
        String body = "{\"description\":\"nova desc\",\"status\":\"IN_EVALUATION\",\"customer\":"
                + order.getCustomer().getId() + ",\"device\":" + order.getDevice().getId() + "}";

        mockMvc.perform(put("/repair-order/{id}", order.getId())
                        .header("Authorization", "Bearer " + atendenteToken)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk());

        assertThat(repairOrderRepository.findById(order.getId()))
                .get()
                .extracting(RepairOrder::getStatus)
                .isEqualTo(RepairOrderStatus.AWAITING_EVALUATION);
    }

    @Test
    void atendenteCannotDeleteRepairOrderGerenteCan() throws Exception {
        RepairOrder order = createRepairOrder("SN-RO-DEL", "rodel@mail.com");

        mockMvc.perform(delete("/repair-order/{id}", order.getId()).header("Authorization", "Bearer " + atendenteToken))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/repair-order/{id}", order.getId()).header("Authorization", "Bearer " + gerenteToken))
                .andExpect(status().isNoContent());
        assertThat(countSoftDeleted("repair_orders", order.getId())).isEqualTo(1);
    }

    // --- Payment ---------------------------------------------------------

    @Test
    void atendenteManagesPaymentsTecnicoCannot() throws Exception {
        RepairOrder order = createRepairOrder("SN-PAY-1", "pay1@mail.com");
        order.setStatus(RepairOrderStatus.APPROVED);
        repairOrderRepository.save(order);
        String body = "{\"amount\":150.00,\"method\":\"CASH\",\"repairOrder\":" + order.getId() + "}";

        mockMvc.perform(post("/payment").header("Authorization", "Bearer " + tecnicoToken)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/payment").header("Authorization", "Bearer " + atendenteToken)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());
        Long paymentId = paymentRepository.findByRepairOrderId(order.getId()).orElseThrow().getId();

        mockMvc.perform(patch("/payment/{id}/status", paymentId).header("Authorization", "Bearer " + atendenteToken)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"APPROVED\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void cannotRegisterPaymentBeforeRepairOrderBudgetApproved() throws Exception {
        RepairOrder order = createRepairOrder("SN-PAY-EARLY", "payearly@mail.com");
        String body = "{\"amount\":150.00,\"method\":\"CASH\",\"repairOrder\":" + order.getId() + "}";

        mockMvc.perform(post("/payment").header("Authorization", "Bearer " + atendenteToken)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isUnprocessableEntity());

        assertThat(paymentRepository.findByRepairOrderId(order.getId())).isEmpty();
    }

    @Test
    void atendenteCannotDeletePaymentGerenteCan() throws Exception {
        Payment payment = createPayment("SN-PAY-DEL", "paydel@mail.com");

        mockMvc.perform(delete("/payment/{id}", payment.getId()).header("Authorization", "Bearer " + atendenteToken))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/payment/{id}", payment.getId()).header("Authorization", "Bearer " + gerenteToken))
                .andExpect(status().isNoContent());
        // Payment não usa @SoftDelete — a remoção é física.
        entityManager.flush();
        assertThat(jdbcTemplate.queryForObject(
                "SELECT count(*) FROM payments WHERE id = ?", Long.class, payment.getId())).isZero();
    }

    // --- User (ADMIN-only, GERENTE excluído) ----------------------------

    @Test
    void onlyAdminListsUsers() throws Exception {
        mockMvc.perform(get("/user").header("Authorization", "Bearer " + atendenteToken))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/user").header("Authorization", "Bearer " + gerenteToken))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/user").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    void onlyAdminUpdatesUserRole() throws Exception {
        User target = userRepository.save(User.builder()
                .name("Alvo").email("alvo@mail.com").password("senha").role(Role.PENDENTE).build());

        mockMvc.perform(patch("/user/{id}/role", target.getId()).header("Authorization", "Bearer " + gerenteToken)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"role\":\"ATENDENTE\"}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(patch("/user/{id}/role", target.getId()).header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"role\":\"ATENDENTE\"}"))
                .andExpect(status().isOk());
    }

    // --- Disabled account -----------------------------------------------

    @Test
    void shouldRejectLoginForDisabledUser() throws Exception {
        userRepository.save(User.builder()
                .name("Nao Ativado").email("nao.ativado@mail.com")
                .password(passwordEncoder.encode("senha123"))
                .role(Role.ATENDENTE).enabled(false).build());

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
                .role(Role.ATENDENTE).enabled(false).build());
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

    // --- Helpers -------------------------------------------------------

    private Device device(String serialNumber) {
        Brand brand = brandRepository.save(Brand.builder().name("Brand-" + serialNumber).build());
        return deviceRepository.save(Device.builder()
                .model("Modelo").serialNumber(serialNumber).brand(brand).accessories(List.of()).build());
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
        Device device = device(serialNumber);
        Customer customer = customerRepository.save(Customer.builder()
                .name("Cliente").phone("11999990009").email(customerEmail).build());
        return repairOrderRepository.save(RepairOrder.builder()
                .description("Reparo")
                .status(RepairOrderStatus.AWAITING_EVALUATION)
                .customer(customer)
                .device(device)
                .build());
    }

    private RepairOrder createRepairOrderReadyForCollection(String serialNumber, String customerEmail) {
        RepairOrder order = createRepairOrder(serialNumber, customerEmail);
        order.setStatus(RepairOrderStatus.REPAIR_COMPLETED);
        repairOrderRepository.save(order);
        paymentRepository.save(Payment.builder()
                .amount(new BigDecimal("150.00"))
                .method(PaymentMethod.CASH)
                .status(PaymentStatus.APPROVED)
                .installments(1)
                .repairOrder(order)
                .build());
        // O OneToOne RepairOrder.payment é o lado inverso (mappedBy) — o Payment recém
        // salvo não atualiza o `order` já gerenciado no contexto de persistência. Limpar
        // o contexto força o próximo findById (dentro do service) a recarregar do banco.
        entityManager.flush();
        entityManager.clear();
        return order;
    }
}
