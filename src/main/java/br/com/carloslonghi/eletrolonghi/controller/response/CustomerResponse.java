package br.com.carloslonghi.eletrolonghi.controller.response;

import lombok.Builder;

@Builder
public record CustomerResponse(
        Long id,
        String name,
        String phone,
        String email
) {
}
