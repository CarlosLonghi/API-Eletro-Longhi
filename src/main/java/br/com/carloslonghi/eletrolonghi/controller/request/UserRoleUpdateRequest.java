package br.com.carloslonghi.eletrolonghi.controller.request;

import br.com.carloslonghi.eletrolonghi.entity.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Dados para atualizar o papel (role) de um usuário")
public record UserRoleUpdateRequest(
        @Schema(description = "Novo papel do usuário", enumAsRef = true)
        @NotNull(message = "O papel não pode ser nulo")
        Role role
) {
}
