package br.com.carloslonghi.eletrolonghi.client;

import br.com.carloslonghi.eletrolonghi.client.dto.GatewayPaymentSnapshot;
import br.com.carloslonghi.eletrolonghi.config.MercadoPagoProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Optional;

/**
 * Esqueleto do client HTTP do Mercado Pago.
 *
 * <p>Na v1 o módulo de pagamentos registra tudo manualmente; este client existe para a
 * integração futura (maquininha via <em>API Point</em> e/ou Checkout, com confirmação por
 * webhook ou polling). O único método já implementado é {@link #getPayment(String)}, que
 * suporta o fluxo de polling — consultar a situação de um pagamento quando não há webhook.
 *
 * <p>TODO (quando a aplicação estiver hospedada): {@code createPointPaymentIntent(...)} para
 * acionar a maquininha e {@code createCheckoutPreference(...)} para gerar link de pagamento.
 */
@Component
public class MercadoPagoClient {

    private static final Logger log = LoggerFactory.getLogger(MercadoPagoClient.class);

    private final RestClient restClient;
    private final boolean configured;

    @Autowired
    public MercadoPagoClient(MercadoPagoProperties properties) {
        this(RestClient.builder()
                        .baseUrl(properties.baseUrl())
                        .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + safeToken(properties.accessToken()))
                        .build(),
                properties.accessToken() != null && !properties.accessToken().isBlank());
    }

    MercadoPagoClient(RestClient restClient, boolean configured) {
        this.restClient = restClient;
        this.configured = configured;
    }

    /**
     * Consulta a situação de um pagamento no Mercado Pago ({@code GET /v1/payments/{id}}).
     * Retorna vazio se o client não está configurado (sem access token) ou se a chamada falha.
     */
    public Optional<GatewayPaymentSnapshot> getPayment(String gatewayPaymentId) {
        if (!configured) {
            log.debug("MercadoPagoClient sem access token configurado; getPayment({}) ignorado.", gatewayPaymentId);
            return Optional.empty();
        }

        try {
            GatewayPaymentSnapshot snapshot = restClient.get()
                    .uri("/v1/payments/{id}", gatewayPaymentId)
                    .retrieve()
                    .body(GatewayPaymentSnapshot.class);
            return Optional.ofNullable(snapshot);
        } catch (RestClientException exception) {
            log.warn("Falha ao consultar pagamento {} no Mercado Pago: {}", gatewayPaymentId, exception.getMessage());
            return Optional.empty();
        }
    }

    private static String safeToken(String token) {
        return token == null ? "" : token;
    }
}
