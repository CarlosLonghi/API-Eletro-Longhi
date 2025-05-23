package br.com.carloslonghi.eletrolonghi.controller.response;

import lombok.Builder;

@Builder
public record BrandResponse(Long id, String name) {
}
