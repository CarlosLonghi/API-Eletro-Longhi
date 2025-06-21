package br.com.carloslonghi.eletrolonghi.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Dados para criar ou atualizar um cliente")
public record CustomerRequest(
        @NotBlank(message = "O nome do cliente é obrigatório.")
        String name,

        @NotBlank(message = "O telefone do cliente é obrigatório.")
        String phone,

        String email
) {
}
