package br.com.carloslonghi.eletrolonghi.controller.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(description = "Detalhes de um acessório retornado pela API")
@Builder
public record AccessoryResponse(
        @Schema(description = "Identificador único do acessório")
        Long id,

        String name
) {
}
