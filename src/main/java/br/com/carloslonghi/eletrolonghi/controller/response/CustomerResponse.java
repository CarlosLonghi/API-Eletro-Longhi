package br.com.carloslonghi.eletrolonghi.controller.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(description = "Detalhes de um cliente retornado pela API")
@Builder
public record CustomerResponse(
        @Schema(description = "Identificador único do cliente")
        Long id,

        String name,
        String phone,
        String email
) {
}
