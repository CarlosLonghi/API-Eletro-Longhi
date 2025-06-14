package br.com.carloslonghi.eletrolonghi.controller.request;

import jakarta.validation.constraints.NotBlank;

public record AccessoryRequest(
        @NotBlank(message = "Accessory 'name' is required.")
        String name
) {
}
