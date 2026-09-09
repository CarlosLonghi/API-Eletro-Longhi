package br.com.carloslonghi.eletrolonghi.client.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Dados do pagador enviados na preference do Checkout Pro ({@code payer}).
 *
 * <p>Preencher isto ajuda o Mercado Pago a oferecer o Pix (e casa melhor o pagamento
 * na conciliação por polling). Campos nulos são omitidos do JSON.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PreferencePayer(
        String name,
        String surname,
        String email,
        PreferenceIdentification identification
) {
}
