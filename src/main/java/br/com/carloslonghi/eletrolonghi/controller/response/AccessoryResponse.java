package br.com.carloslonghi.eletrolonghi.controller.response;

import lombok.Builder;

@Builder
public record AccessoryResponse(Long id, String name) {
}
