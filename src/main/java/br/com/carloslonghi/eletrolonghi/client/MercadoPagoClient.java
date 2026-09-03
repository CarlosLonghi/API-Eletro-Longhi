package br.com.carloslonghi.eletrolonghi.client;

import br.com.carloslonghi.eletrolonghi.client.dto.CheckoutPreference;
import br.com.carloslonghi.eletrolonghi.client.dto.GatewayPaymentSnapshot;
import br.com.carloslonghi.eletrolonghi.client.dto.PaymentSearchResponse;
import br.com.carloslonghi.eletrolonghi.client.dto.PreferenceItem;
import br.com.carloslonghi.eletrolonghi.client.dto.PreferenceRequest;
import br.com.carloslonghi.eletrolonghi.config.MercadoPagoProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Client HTTP do Mercado Pago.
 *
 * <p>Suporta o fluxo <strong>Checkout Pro</strong> sem webhook:
 * {@link #createCheckoutPreference} gera o link de pagamento e
 * {@link #findPaymentByExternalReference} concilia a situação por polling.
 * {@link #getPayment} consulta um pagamento por id.
 *
 * <p>Todo método retorna {@code Optional.empty()} quando o client não está configurado
 * (sem {@code mercadopago.access-token}) ou quando a chamada falha — cabe ao chamador
 * decidir o efeito (ex.: {@code PaymentGatewayException}).
 *
 * <p>TODO (maquininha física, quando houver credenciais de produção + device Point Smart):
 * {@code createPointPaymentIntent(...)}.
 */
@Component
public class MercadoPagoClient {

    private static final Logger log = LoggerFactory.getLogger(MercadoPagoClient.class);
    private static final String CURRENCY_BRL = "BRL";

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
     * Cria uma preference do Checkout Pro ({@code POST /checkout/preferences}) com um único
     * item e devolve o link ({@code init_point}) para o cliente pagar.
     */
    public Optional<CheckoutPreference> createCheckoutPreference(String title, BigDecimal amount, String externalReference) {
        if (notConfigured("createCheckoutPreference")) {
            return Optional.empty();
        }

        PreferenceRequest body = new PreferenceRequest(
                List.of(new PreferenceItem(title, 1, amount, CURRENCY_BRL)),
                externalReference
        );

        try {
            CheckoutPreference preference = restClient.post()
                    .uri("/checkout/preferences")
                    .body(body)
                    .retrieve()
                    .body(CheckoutPreference.class);
            return Optional.ofNullable(preference);
        } catch (RestClientException exception) {
            log.warn("Falha ao criar preference no Mercado Pago (ref {}): {}", externalReference, exception.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Busca o pagamento mais recente vinculado a um {@code external_reference}
     * ({@code GET /v1/payments/search}). Usado para conciliar por polling.
     */
    public Optional<GatewayPaymentSnapshot> findPaymentByExternalReference(String externalReference) {
        if (notConfigured("findPaymentByExternalReference")) {
            return Optional.empty();
        }

        try {
            PaymentSearchResponse search = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/v1/payments/search")
                            .queryParam("external_reference", externalReference)
                            .queryParam("sort", "date_created")
                            .queryParam("criteria", "desc")
                            .build())
                    .retrieve()
                    .body(PaymentSearchResponse.class);

            if (search == null || search.results() == null || search.results().isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(search.results().get(0));
        } catch (RestClientException exception) {
            log.warn("Falha ao buscar pagamento por ref {} no Mercado Pago: {}", externalReference, exception.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Consulta a situação de um pagamento no Mercado Pago ({@code GET /v1/payments/{id}}).
     */
    public Optional<GatewayPaymentSnapshot> getPayment(String gatewayPaymentId) {
        if (notConfigured("getPayment")) {
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

    private boolean notConfigured(String operation) {
        if (!configured) {
            log.debug("MercadoPagoClient sem access token configurado; {} ignorado.", operation);
            return true;
        }
        return false;
    }

    private static String safeToken(String token) {
        return token == null ? "" : token;
    }
}
