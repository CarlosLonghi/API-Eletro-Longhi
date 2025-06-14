package br.com.carloslonghi.eletrolonghi.controller.request;

import jakarta.validation.constraints.NotBlank;

public record CustomerRequest(
        @NotBlank(message = "User 'name' is required.")
        String name,

        @NotBlank(message = "User 'phone' is required.")
        String phone,

        String email
) {
}
