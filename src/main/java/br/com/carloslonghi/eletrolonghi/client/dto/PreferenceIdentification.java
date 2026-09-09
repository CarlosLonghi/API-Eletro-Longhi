package br.com.carloslonghi.eletrolonghi.client.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/** Documento do pagador dentro de {@link PreferencePayer} ({@code payer.identification}). */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PreferenceIdentification(
        String type,
        String number
) {
}
