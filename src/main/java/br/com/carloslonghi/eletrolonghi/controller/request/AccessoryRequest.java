package br.com.carloslonghi.eletrolonghi.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Dados para criar ou atualizar um acessório")
public record AccessoryRequest (
        @NotBlank(message = "O nome do acessório é obrigatório.")
        String name
) {
}
