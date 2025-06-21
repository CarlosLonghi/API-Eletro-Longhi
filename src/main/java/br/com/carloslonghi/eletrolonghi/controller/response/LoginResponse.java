package br.com.carloslonghi.eletrolonghi.controller.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Token de autenticação retornado após login")
public record LoginResponse(
        @Schema(description = "Token JWT para autenticação")
        String token
) {
}
