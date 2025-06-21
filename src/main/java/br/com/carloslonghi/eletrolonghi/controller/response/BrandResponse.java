package br.com.carloslonghi.eletrolonghi.controller.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(description = "Detalhes de uma marca retornada pela API")
@Builder
public record BrandResponse(
        @Schema(description = "Identificador único da marca")
        Long id,

        String name
) {
}
