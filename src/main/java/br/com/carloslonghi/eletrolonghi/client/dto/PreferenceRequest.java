package br.com.carloslonghi.eletrolonghi.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/** Corpo enviado ao Mercado Pago para criar uma preference do Checkout Pro. */
public record PreferenceRequest(
        List<PreferenceItem> items,
        @JsonProperty("external_reference") String externalReference
) {
}
