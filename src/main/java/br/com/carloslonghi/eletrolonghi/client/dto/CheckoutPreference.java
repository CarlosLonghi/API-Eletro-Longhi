package br.com.carloslonghi.eletrolonghi.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Recorte da resposta de {@code POST /checkout/preferences}. {@code initPoint} é o link
 * para o checkout hospedado do Mercado Pago (produção); {@code sandboxInitPoint} para testes.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CheckoutPreference(
        String id,
        @JsonProperty("init_point") String initPoint,
        @JsonProperty("sandbox_init_point") String sandboxInitPoint
) {
}
