package br.com.carloslonghi.eletrolonghi.controller.request;

import br.com.carloslonghi.eletrolonghi.entity.enums.RepairOrderStatus;
import jakarta.validation.constraints.NotNull;

public record RepairOrderRequest(
        String description,

        @NotNull(message = "RepairOrder 'status' is required.")
        RepairOrderStatus status,

        @NotNull(message = "RepairOrder 'customer' is required.")
        Long customer,

        @NotNull(message = "RepairOrder 'device' is required.")
        Long device
) {
}
