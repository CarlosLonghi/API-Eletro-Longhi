package br.com.carloslonghi.eletrolonghi.controller.response;

import br.com.carloslonghi.eletrolonghi.entity.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(description = "Detalhes de um usuário retornado pela API")
@Builder
public record UserResponse(
        @Schema(description = "Identificador único do usuário", example = "1")
        Long id,

        @Schema(description = "Nome do usuário", example = "Maria Souza")
        String name,

        @Schema(description = "E-mail do usuário", example = "maria.souza@email.com")
        String email,

        @Schema(description = "Papel do usuário na aplicação")
        Role role,

        @Schema(description = "Indica se o usuário está habilitado a acessar o sistema", example = "true")
        boolean enabled
) {
}
