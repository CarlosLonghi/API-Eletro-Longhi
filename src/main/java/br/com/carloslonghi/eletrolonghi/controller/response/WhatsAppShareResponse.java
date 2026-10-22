package br.com.carloslonghi.eletrolonghi.controller.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "Link do WhatsApp (wa.me) com a mensagem do comprovante pronta para o cliente")
public record WhatsAppShareResponse(
        @Schema(description = "Telefone do cliente no formato do WhatsApp (DDI + DDD + número)", example = "5511987654321")
        String phone,
        @Schema(description = "Mensagem que será preenchida na conversa")
        String message,
        @Schema(description = "Link que abre a conversa com a mensagem preenchida",
                example = "https://wa.me/5511987654321?text=Ol%C3%A1%2C%20Maria...")
        String url
) {
}
