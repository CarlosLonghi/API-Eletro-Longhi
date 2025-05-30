package br.com.carloslonghi.eletrolonghi.controller.response;

import br.com.carloslonghi.eletrolonghi.entity.enums.RepairOrderStatus;
import lombok.Builder;

@Builder
public record RepairOrderResponse(
        Long id,
        String description,
        RepairOrderStatus status,
        CustomerResponse customer,
        DeviceResponse device
) {
}
