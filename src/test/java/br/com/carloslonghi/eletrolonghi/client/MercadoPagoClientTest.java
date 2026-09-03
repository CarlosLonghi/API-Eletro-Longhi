package br.com.carloslonghi.eletrolonghi.client;

import br.com.carloslonghi.eletrolonghi.client.dto.CheckoutPreference;
import br.com.carloslonghi.eletrolonghi.client.dto.GatewayPaymentSnapshot;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class MercadoPagoClientTest {

    private MockRestServiceServer server;

    private MercadoPagoClient configuredClient() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://api.mercadopago.com");
        server = MockRestServiceServer.bindTo(builder).build();
        return new MercadoPagoClient(builder.build(), true);
    }

    @Test
    void shouldParsePaymentSnapshot() {
        MercadoPagoClient client = configuredClient();

        server.expect(requestTo("https://api.mercadopago.com/v1/payments/123"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {"id":123,"status":"approved","status_detail":"accredited","external_reference":"PAY-1"}
                        """, MediaType.APPLICATION_JSON));

        Optional<GatewayPaymentSnapshot> snapshot = client.getPayment("123");

        assertThat(snapshot).isPresent();
        assertThat(snapshot.get().status()).isEqualTo("approved");
        assertThat(snapshot.get().externalReference()).isEqualTo("PAY-1");
        server.verify();
    }

    @Test
    void shouldReturnEmptyWhenNotConfigured() {
        MercadoPagoClient client = new MercadoPagoClient(RestClient.create(), false);

        assertThat(client.getPayment("123")).isEmpty();
        assertThat(client.createCheckoutPreference("t", BigDecimal.TEN, "payment-1")).isEmpty();
        assertThat(client.findPaymentByExternalReference("payment-1")).isEmpty();
    }

    @Test
    void shouldReturnEmptyOnGatewayError() {
        MercadoPagoClient client = configuredClient();

        server.expect(requestTo("https://api.mercadopago.com/v1/payments/999"))
                .andRespond(withServerError());

        assertThat(client.getPayment("999")).isEmpty();
        server.verify();
    }

    @Test
    void shouldCreateCheckoutPreference() {
        MercadoPagoClient client = configuredClient();

        server.expect(requestTo("https://api.mercadopago.com/checkout/preferences"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(jsonPath("$.external_reference").value("payment-1"))
                .andExpect(jsonPath("$.items[0].unit_price").value(350.0))
                .andExpect(jsonPath("$.items[0].currency_id").value("BRL"))
                .andRespond(withSuccess("""
                        {"id":"pref-1","init_point":"https://mp/checkout","sandbox_init_point":"https://mp/sandbox"}
                        """, MediaType.APPLICATION_JSON));

        Optional<CheckoutPreference> preference =
                client.createCheckoutPreference("Reparo #1", new BigDecimal("350.00"), "payment-1");

        assertThat(preference).isPresent();
        assertThat(preference.get().initPoint()).isEqualTo("https://mp/checkout");
        server.verify();
    }

    @Test
    void shouldFindPaymentByExternalReference() {
        MercadoPagoClient client = configuredClient();

        server.expect(requestTo(containsString("/v1/payments/search")))
                .andExpect(method(HttpMethod.GET))
                .andExpect(queryParam("external_reference", "payment-1"))
                .andExpect(queryParam("criteria", "desc"))
                .andRespond(withSuccess("""
                        {"results":[{"id":999,"status":"approved","status_detail":"accredited","external_reference":"payment-1"}]}
                        """, MediaType.APPLICATION_JSON));

        Optional<GatewayPaymentSnapshot> snapshot = client.findPaymentByExternalReference("payment-1");

        assertThat(snapshot).isPresent();
        assertThat(snapshot.get().id()).isEqualTo(999L);
        assertThat(snapshot.get().status()).isEqualTo("approved");
        server.verify();
    }

    @Test
    void shouldReturnEmptyWhenSearchHasNoResults() {
        MercadoPagoClient client = configuredClient();

        server.expect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"results\":[]}", MediaType.APPLICATION_JSON));

        assertThat(client.findPaymentByExternalReference("payment-x")).isEmpty();
        server.verify();
    }
}
