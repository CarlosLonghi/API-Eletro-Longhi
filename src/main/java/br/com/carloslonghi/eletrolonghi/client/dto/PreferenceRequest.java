package br.com.carloslonghi.eletrolonghi.client.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/** Corpo enviado ao Mercado Pago para criar uma preference do Checkout Pro. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PreferenceRequest(
        List<PreferenceItem> items,
        @JsonProperty("external_reference") String externalReference,
        PreferencePayer payer
) {
}
