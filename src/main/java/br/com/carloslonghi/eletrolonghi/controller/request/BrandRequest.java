package br.com.carloslonghi.eletrolonghi.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Dados para criar ou atualizar uma marca")
public record BrandRequest (
        @NotBlank(message = "O nome da marca é obrigatório.")
        String name
) {
}
