package br.com.carloslonghi.eletrolonghi.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Dados para criar ou atualizar um cliente")
public record CustomerRequest(
        @Schema(description = "Nome do cliente", example = "João da Silva")
        @NotBlank(message = "O nome do cliente é obrigatório.")
        String name,

        @Schema(description = "Telefone do cliente", example = "(11) 91234-5678")
        @NotBlank(message = "O telefone do cliente é obrigatório.")
        String phone,

        @Schema(description = "E-mail do cliente", example = "joao.silva@email.com")
        String email
) {
}
