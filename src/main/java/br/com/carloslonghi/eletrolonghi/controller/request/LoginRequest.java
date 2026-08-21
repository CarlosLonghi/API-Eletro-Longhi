package br.com.carloslonghi.eletrolonghi.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Credenciais para autenticação de usuário")
public record LoginRequest(
        @Schema(description = "Endereço de e-mail do usuário", example = "maria.souza@email.com")
        @NotBlank(message = "O e-mail do usuário é obrigatório.")
        String email,

        @Schema(description = "Senha do usuário", example = "SenhaForte123")
        @NotBlank(message = "A senha do usuário é obrigatória.")
        String password
) {
}
