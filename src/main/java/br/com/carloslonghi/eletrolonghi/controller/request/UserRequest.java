package br.com.carloslonghi.eletrolonghi.controller.request;

public record UserRequest(
        String name,
        String email,
        String password
) {
}
