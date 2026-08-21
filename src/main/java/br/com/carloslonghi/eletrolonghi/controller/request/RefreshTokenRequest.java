package br.com.carloslonghi.eletrolonghi.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Requisição para renovar o token de acesso usando um refresh token")
public record RefreshTokenRequest(
        @Schema(description = "Refresh token emitido no login", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        @NotBlank(message = "O refresh token é obrigatório.")
        String refreshToken
) {
}

