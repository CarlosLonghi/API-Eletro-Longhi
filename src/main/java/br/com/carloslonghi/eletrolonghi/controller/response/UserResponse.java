package br.com.carloslonghi.eletrolonghi.controller.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(description = "Detalhes de um usuário retornado pela API")
@Builder
public record UserResponse(
        @Schema(description = "Identificador único do usuário")
        Long id,

        String name,
        String email
) {
}
