package br.com.carloslonghi.eletrolonghi.controller.request;

import java.util.List;

public record DeviceRequest(
        String model,
        String serialNumber,
        Long brand,
        List<Long> accessories
) {
}
