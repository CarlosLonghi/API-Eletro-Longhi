package br.com.carloslonghi.eletrolonghi.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Recorte da resposta de {@code GET /v1/payments/{id}} do Mercado Pago — o mínimo
 * necessário para reconciliar um {@code Payment} local (usado no polling futuro).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record GatewayPaymentSnapshot(
        Long id,
        String status,
        @JsonProperty("status_detail") String statusDetail,
        @JsonProperty("external_reference") String externalReference
) {
}
