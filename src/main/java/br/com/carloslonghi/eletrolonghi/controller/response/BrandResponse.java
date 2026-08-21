package br.com.carloslonghi.eletrolonghi.controller.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(description = "Detalhes de uma marca retornada pela API")
@Builder
public record BrandResponse(
        @Schema(description = "Identificador único da marca", example = "1")
        Long id,

        @Schema(description = "Nome da marca", example = "Samsung")
        String name
) {
}
