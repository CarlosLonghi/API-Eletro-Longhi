package br.com.carloslonghi.eletrolonghi.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Requisição para renovar o token de acesso usando um refresh token")
public record RefreshTokenRequest(
        @Schema(description = "Refresh token emitido no login")
        @NotBlank(message = "O refresh token é obrigatório.")
        String refreshToken
) {
}

