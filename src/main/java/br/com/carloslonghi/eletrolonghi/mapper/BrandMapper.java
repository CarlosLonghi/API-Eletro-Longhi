package br.com.carloslonghi.eletrolonghi.mapper;

import br.com.carloslonghi.eletrolonghi.controller.request.BrandRequest;
import br.com.carloslonghi.eletrolonghi.controller.response.BrandResponse;
import br.com.carloslonghi.eletrolonghi.entity.Brand;
import lombok.experimental.UtilityClass;

@UtilityClass
public class BrandMapper {

    public static Brand toBrandEntity(BrandRequest request) {
        return Brand
                .builder()
                .name(request.name())
                .build();
    }

    public static BrandResponse toBrandResponse(Brand response) {
        return BrandResponse
                .builder()
                .id(response.getId())
                .name(response.getName())
                .build();
    }
}
