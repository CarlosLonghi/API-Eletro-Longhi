package br.com.carloslonghi.eletrolonghi.controller.request;

import jakarta.validation.constraints.NotBlank;

public record BrandRequest (
        @NotBlank(message = "Brand 'name' is required.")
        String name
) {
}
