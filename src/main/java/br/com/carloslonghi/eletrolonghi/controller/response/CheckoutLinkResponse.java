package br.com.carloslonghi.eletrolonghi.controller.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "Link de pagamento do Checkout Pro gerado para um pagamento")
public record CheckoutLinkResponse(
        @Schema(description = "URL do checkout hospedado do Mercado Pago (enviar ao cliente)",
                example = "https://www.mercadopago.com.br/checkout/v1/redirect?pref_id=...")
        String initPoint
) {
}
