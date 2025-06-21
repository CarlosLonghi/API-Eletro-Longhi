package br.com.carloslonghi.eletrolonghi.controller.request;

import br.com.carloslonghi.eletrolonghi.entity.enums.RepairOrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Dados para criar ou atualizar um serviço de reparo")
public record RepairOrderRequest(
        @Schema(description = "Detalhes do serviço solicitado")
        String description,

        @Schema(description = "Estado atual do serviço", enumAsRef = true)
        @NotNull(message = "RepairOrder 'status' is required.")
        RepairOrderStatus status,

        @Schema(description = "ID do cliente que solicitou o serviço")
        @NotNull(message = "RepairOrder 'customer' is required.")
        Long customer,

        @Schema(description = "ID do aparelho para o serviço")
        @NotNull(message = "RepairOrder 'device' is required.")
        Long device
) {
}
