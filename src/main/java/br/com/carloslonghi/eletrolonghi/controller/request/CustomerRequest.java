package br.com.carloslonghi.eletrolonghi.controller.request;

public record CustomerRequest(
        String name,
        String phone,
        String email
) {
}
