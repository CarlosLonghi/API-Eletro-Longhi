package br.com.carloslonghi.eletrolonghi.controller.request;

import jakarta.validation.constraints.NotBlank;

public record UserRequest(
        @NotBlank(message = "User 'name' is required.")
        String name,

        @NotBlank(message = "User 'email' is required.")
        String email,

        @NotBlank(message = "User 'password' is required.")
        String password
) {
}
