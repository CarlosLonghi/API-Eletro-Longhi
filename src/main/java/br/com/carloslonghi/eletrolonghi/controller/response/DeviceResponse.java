package br.com.carloslonghi.eletrolonghi.controller.response;

import lombok.Builder;

import java.util.List;

@Builder
public record DeviceResponse(
        Long id,
        String model,
        String serialNumber,
        BrandResponse brand,
        List<AccessoryResponse> accessories
) {
}
