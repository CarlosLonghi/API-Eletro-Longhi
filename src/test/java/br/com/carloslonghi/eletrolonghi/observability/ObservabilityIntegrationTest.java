package br.com.carloslonghi.eletrolonghi.observability;

import br.com.carloslonghi.eletrolonghi.repository.support.AbstractPostgresIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalManagementPort;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Actuator na porta de gerência (separada da API): health liberado sem token e /actuator/prometheus
 * com as métricas de negócio (registradas zeradas no startup), o histograma do HTTP e o pool do banco.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ObservabilityIntegrationTest extends AbstractPostgresIntegrationTest {

    @LocalServerPort
    private int port;

    @LocalManagementPort
    private int managementPort;

    private String management(String path) {
        return RestClient.create().get()
                .uri("http://localhost:" + managementPort + path)
                .retrieve()
                .body(String.class);
    }

    @Test
    void shouldServeHealthWithoutToken() {
        assertThat(managementPort).isNotEqualTo(port);
        assertThat(management("/actuator/health")).contains("\"status\":\"UP\"");
    }

    @Test
    void shouldExposeBusinessAndInfraMetrics() {
        // Uma requisição na API para o histograma do HTTP e o contador de login ganharem amostra
        RestClient.create().post()
                .uri("http://localhost:" + port + "/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"email\":\"nobody@eletrolonghi.com\",\"password\":\"wrong-password\"}")
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> { })
                .toBodilessEntity();

        assertThat(management("/actuator/prometheus"))
                .contains("eletrolonghi_repair_orders_opened_total ")
                .contains("eletrolonghi_repair_orders_status_changes_total{status=\"DEVICE_COLLECTED\"}")
                .contains("eletrolonghi_repair_orders_by_status{status=\"AWAITING_EVALUATION\"}")
                .contains("eletrolonghi_payments_registered_total{method=\"PIX\"}")
                .contains("eletrolonghi_payments_approved_amount_brl_sum{method=\"CARD\"}")
                .contains("eletrolonghi_payments_checkout_links_total ")
                .contains("eletrolonghi_auth_logins_total{result=\"failure\"}")
                .contains("http_server_requests_seconds_bucket{")
                .contains("hikaricp_connections_active");
    }
}
