package br.com.carloslonghi.eletrolonghi.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record DeviceRequest(
        @NotBlank(message = "Device 'model' is required.")
        String model,

        String serialNumber,

        @NotNull(message = "Device 'brand' is required.")
        Long brand,

        @NotNull(message = "Device 'accessories' is required.")
        List<Long> accessories
) {
}
