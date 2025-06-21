package br.com.carloslonghi.eletrolonghi.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Dados para criar ou atualizar um usuário")
public record UserRequest(
        @NotBlank(message = "O nome do usuário é obrigatório.")
        String name,

        @NotBlank(message = "O e-mail do usuário é obrigatório.")
        String email,

        @NotBlank(message = "A senha do usuário é obrigatória.")
        String password
) {
}
