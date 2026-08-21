package br.com.carloslonghi.eletrolonghi.controller.request;

import br.com.carloslonghi.eletrolonghi.entity.enums.RepairOrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Dados para atualizar o status de uma ordem de reparo")
public record RepairOrderStatusUpdateRequest(
        @Schema(description = "Novo estado do serviço", enumAsRef = true)
        @NotNull(message = "O status não pode ser nulo")
        RepairOrderStatus status
) {
}

