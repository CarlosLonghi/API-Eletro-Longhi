package br.com.carloslonghi.eletrolonghi.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/** Item de uma preference do Checkout Pro ({@code POST /checkout/preferences}). */
public record PreferenceItem(
        String title,
        int quantity,
        @JsonProperty("unit_price") BigDecimal unitPrice,
        @JsonProperty("currency_id") String currencyId
) {
}
