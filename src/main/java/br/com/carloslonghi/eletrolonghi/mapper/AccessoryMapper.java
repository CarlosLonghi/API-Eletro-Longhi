package br.com.carloslonghi.eletrolonghi.mapper;

import br.com.carloslonghi.eletrolonghi.controller.request.AccessoryRequest;
import br.com.carloslonghi.eletrolonghi.controller.response.AccessoryResponse;
import br.com.carloslonghi.eletrolonghi.entity.Accessory;
import lombok.experimental.UtilityClass;

@UtilityClass
public class AccessoryMapper {

    public static Accessory toAccessoryEntity(AccessoryRequest request) {
        return Accessory
                .builder()
                .name(request.name())
                .build();
    }

    public static AccessoryResponse toAccessoryResponse(Accessory response) {
        return AccessoryResponse
                .builder()
                .id(response.getId())
                .name(response.getName())
                .build();
    }
}
