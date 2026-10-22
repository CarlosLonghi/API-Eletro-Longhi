package br.com.carloslonghi.eletrolonghi.controller;

import br.com.carloslonghi.eletrolonghi.config.TokenService;
import br.com.carloslonghi.eletrolonghi.entity.Brand;
import br.com.carloslonghi.eletrolonghi.entity.Customer;
import br.com.carloslonghi.eletrolonghi.entity.Device;
import br.com.carloslonghi.eletrolonghi.entity.RepairOrder;
import br.com.carloslonghi.eletrolonghi.entity.User;
import br.com.carloslonghi.eletrolonghi.entity.enums.RepairOrderStatus;
import br.com.carloslonghi.eletrolonghi.entity.enums.Role;
import br.com.carloslonghi.eletrolonghi.repository.BrandRepository;
import br.com.carloslonghi.eletrolonghi.repository.CustomerRepository;
import br.com.carloslonghi.eletrolonghi.repository.DeviceRepository;
import br.com.carloslonghi.eletrolonghi.repository.RepairOrderRepository;
import br.com.carloslonghi.eletrolonghi.repository.UserRepository;
import br.com.carloslonghi.eletrolonghi.repository.support.AbstractPostgresIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Valida o contrato HTTP de erro ponta a ponta (validação, referência inexistente, duplicidade
 * e regras de negócio), passando pela cadeia real de filtros, {@code @Valid} e
 * {@code config/ApplicationControllerAdvice}. A matriz de 401/403 por papel fica em
 * {@code security/AuthorizationIntegrationTest}.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
class ErrorResponseIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private RepairOrderRepository repairOrderRepository;

    @Autowired
    private UserRepository userRepository;

    private String adminToken;

    @BeforeEach
    void setUpToken() {
        adminToken = tokenService.generateToken(User.builder()
                .id(1L).name("Admin").email("admin@mail.com").role(Role.ADMIN).build());
    }

    // --- 400: validação do corpo ---------------------------------------------

    @Test
    void shouldReturnFieldErrorsWhenDeviceBodyIsInvalid() throws Exception {
        mockMvc.perform(post("/device")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"model\":\"Galaxy\",\"serialNumber\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.serialNumber").value("O número de série do aparelho é obrigatório."))
                .andExpect(jsonPath("$.brand").exists())
                .andExpect(jsonPath("$.accessories").exists());
    }

    @Test
    void shouldReturnFieldErrorsWhenCustomerEmailIsInvalid() throws Exception {
        mockMvc.perform(post("/customer")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Ana\",\"phone\":\"11999990000\",\"email\":\"nao-e-email\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email").value("O e-mail do cliente deve ser válido."));
    }

    @Test
    void shouldReturnFieldErrorsWhenRegisterBodyIsEmpty() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").exists())
                .andExpect(jsonPath("$.email").exists())
                .andExpect(jsonPath("$.password").exists());
    }

    // --- 404 -----------------------------------------------------------------

    @Test
    void shouldReturnNotFoundForMissingBrand() throws Exception {
        mockMvc.perform(get("/brand/{id}", 999_999L)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnNotFoundWhenDeviceReferencesMissingBrand() throws Exception {
        mockMvc.perform(post("/device")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"model\":\"Galaxy\",\"serialNumber\":\"SN-ERR-404\",\"brand\":999999,\"accessories\":[]}"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("999999")));
    }

    // --- 409: duplicidade ----------------------------------------------------

    @Test
    void shouldReturnConflictWhenCustomerEmailAlreadyExists() throws Exception {
        customerRepository.save(Customer.builder().name("Ana").phone("11999990000").email("dup@mail.com").build());

        mockMvc.perform(post("/customer")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Outra Ana\",\"phone\":\"11999990001\",\"email\":\"dup@mail.com\"}"))
                .andExpect(status().isConflict())
                .andExpect(content().string(containsString("integridade")));
    }

    // --- 422: regras de negócio ----------------------------------------------

    @Test
    void shouldReturnUnprocessableWhenStatusTransitionSkipsSteps() throws Exception {
        RepairOrder order = createRepairOrder("SN-ERR-SKIP", "skip@mail.com", RepairOrderStatus.AWAITING_EVALUATION);

        mockMvc.perform(patch("/repair-order/{id}/status", order.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"IN_REPAIR\"}"))
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    void shouldReturnUnprocessableWhenAwaitingApprovalWithoutEstimate() throws Exception {
        RepairOrder order = createRepairOrder("SN-ERR-EST", "est@mail.com", RepairOrderStatus.IN_EVALUATION);

        mockMvc.perform(patch("/repair-order/{id}/status", order.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"AWAITING_APPROVAL\"}"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(content().string(containsString("custo estimado")));
    }

    @Test
    void shouldReturnUnprocessableWhenPayingOrderNotYetApproved() throws Exception {
        RepairOrder order = createRepairOrder("SN-ERR-PAY", "pay@mail.com", RepairOrderStatus.IN_EVALUATION);

        mockMvc.perform(post("/payment")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(paymentBody(order)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(content().string(containsString("orçamento ainda não foi aprovado")));
    }

    @Test
    void shouldReturnUnprocessableWhenOrderAlreadyHasPayment() throws Exception {
        RepairOrder order = createRepairOrder("SN-ERR-DUP", "duppay@mail.com", RepairOrderStatus.APPROVED);

        mockMvc.perform(post("/payment")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(paymentBody(order)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/payment")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(paymentBody(order)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(content().string(containsString("já possui um pagamento")));
    }

    // --- 401 / 403: autenticação ---------------------------------------------

    @Test
    void shouldReturnUnauthorizedWhenRefreshTokenIsUnknown() throws Exception {
        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"nao-existe\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnForbiddenWhenLoggingIntoDisabledAccount() throws Exception {
        userRepository.save(User.builder()
                .name("Novo").email("novo@mail.com").password(passwordEncoder.encode("Senha123"))
                .role(Role.PENDENTE).enabled(false).build());

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"novo@mail.com\",\"password\":\"Senha123\"}"))
                .andExpect(status().isForbidden())
                .andExpect(content().string(containsString("aguardando ativação")));
    }

    @Test
    void shouldReturnUnauthorizedWhenPasswordIsWrong() throws Exception {
        userRepository.save(User.builder()
                .name("Ativo").email("ativo@mail.com").password(passwordEncoder.encode("Senha123"))
                .role(Role.ATENDENTE).enabled(true).build());

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"ativo@mail.com\",\"password\":\"errada\"}"))
                .andExpect(status().isUnauthorized());
    }

    private RepairOrder createRepairOrder(String serialNumber, String customerEmail, RepairOrderStatus status) {
        Brand brand = brandRepository.save(Brand.builder().name("Marca-" + serialNumber).build());
        Device device = deviceRepository.save(Device.builder()
                .model("Modelo").serialNumber(serialNumber).brand(brand).accessories(List.of()).build());
        Customer customer = customerRepository.save(Customer.builder()
                .name("Cliente").phone("11999990000").email(customerEmail).build());
        return repairOrderRepository.save(RepairOrder.builder()
                .description("Reparo").status(status).customer(customer).device(device).build());
    }

    private String paymentBody(RepairOrder order) {
        return "{\"amount\":150.00,\"method\":\"CASH\",\"repairOrder\":" + order.getId() + "}";
    }
}
