package br.com.carloslonghi.eletrolonghi.client;

import br.com.carloslonghi.eletrolonghi.client.dto.GatewayPaymentSnapshot;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class MercadoPagoClientTest {

    @Test
    void shouldParsePaymentSnapshot() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://api.mercadopago.com");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        MercadoPagoClient client = new MercadoPagoClient(builder.build(), true);

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
    }

    @Test
    void shouldReturnEmptyOnGatewayError() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://api.mercadopago.com");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        MercadoPagoClient client = new MercadoPagoClient(builder.build(), true);

        server.expect(requestTo("https://api.mercadopago.com/v1/payments/999"))
                .andRespond(withServerError());

        assertThat(client.getPayment("999")).isEmpty();
        server.verify();
    }
}
