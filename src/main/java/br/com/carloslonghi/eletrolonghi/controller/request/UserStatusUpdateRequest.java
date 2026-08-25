package br.com.carloslonghi.eletrolonghi.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Dados para ativar/suspender o acesso de um usuário")
public record UserStatusUpdateRequest(
        @Schema(description = "true para habilitar o acesso, false para suspender", example = "true")
        @NotNull(message = "O campo enabled não pode ser nulo")
        Boolean enabled
) {
}
