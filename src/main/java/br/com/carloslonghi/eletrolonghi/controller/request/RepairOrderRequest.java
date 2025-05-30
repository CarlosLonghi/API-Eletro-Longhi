package br.com.carloslonghi.eletrolonghi.controller.request;

import br.com.carloslonghi.eletrolonghi.entity.enums.RepairOrderStatus;

public record RepairOrderRequest(
        String description,
        RepairOrderStatus status,
        Long customer,
        Long device
) {
}
