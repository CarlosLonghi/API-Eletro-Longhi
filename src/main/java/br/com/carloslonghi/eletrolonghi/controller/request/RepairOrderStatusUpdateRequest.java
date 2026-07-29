package br.com.carloslonghi.eletrolonghi.controller.request;

import br.com.carloslonghi.eletrolonghi.entity.enums.RepairOrderStatus;
import jakarta.validation.constraints.NotNull;

public record RepairOrderStatusUpdateRequest(
        @NotNull(message = "O status não pode ser nulo")
        RepairOrderStatus status
) {
}

