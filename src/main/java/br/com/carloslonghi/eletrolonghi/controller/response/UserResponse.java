package br.com.carloslonghi.eletrolonghi.controller.response;

import br.com.carloslonghi.eletrolonghi.entity.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(description = "Detalhes de um usuário retornado pela API")
@Builder
public record UserResponse(
        @Schema(description = "Identificador único do usuário")
        Long id,

        String name,
        String email,

        @Schema(description = "Papel do usuário na aplicação")
        Role role
) {
}
