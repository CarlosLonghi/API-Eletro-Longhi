package br.com.carloslonghi.eletrolonghi.mapper;

import br.com.carloslonghi.eletrolonghi.controller.request.DeviceRequest;
import br.com.carloslonghi.eletrolonghi.controller.response.DeviceResponse;
import br.com.carloslonghi.eletrolonghi.entity.Accessory;
import br.com.carloslonghi.eletrolonghi.entity.Brand;
import br.com.carloslonghi.eletrolonghi.entity.Device;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.IterableMapping;
import java.util.List;

@Mapper(componentModel = "spring", uses = {AccessoryMapper.class, BrandMapper.class})
public interface DeviceMapper {

    @Mapping(target = "brand", source = "brand", qualifiedByName = "brandFromId")
    @IterableMapping(qualifiedByName = "accessoryFromId")
    Device toEntity(DeviceRequest request);

    DeviceResponse toResponse(Device response);

    @Named("brandFromId")
    default Brand brandFromId(Long id) {
        return id == null ? null : Brand.builder().id(id).build();
    }

    @Named("accessoryFromId")
    default Accessory accessoryFromId(Long id) {
        return id == null ? null : Accessory.builder().id(id).build();
    }

    // explicit List<Long> -> List<Accessory> mapping helper
    default List<Accessory> map(List<Long> ids) {
        if (ids == null) return null;
        return ids.stream().map(this::accessoryFromId).toList();
    }
}
