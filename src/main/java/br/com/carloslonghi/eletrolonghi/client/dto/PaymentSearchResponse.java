package br.com.carloslonghi.eletrolonghi.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/** Recorte da resposta de {@code GET /v1/payments/search}. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PaymentSearchResponse(
        List<GatewayPaymentSnapshot> results
) {
}
