package br.com.carloslonghi.eletrolonghi.mapper;

import br.com.carloslonghi.eletrolonghi.controller.request.RepairOrderRequest;
import br.com.carloslonghi.eletrolonghi.controller.response.RepairOrderResponse;
import br.com.carloslonghi.eletrolonghi.entity.Customer;
import br.com.carloslonghi.eletrolonghi.entity.Device;
import br.com.carloslonghi.eletrolonghi.entity.RepairOrder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring", uses = {DeviceMapper.class, CustomerMapper.class})
public interface RepairOrderMapper {

    @Mapping(target = "customer", source = "customer", qualifiedByName = "customerFromId")
    @Mapping(target = "device", source = "device", qualifiedByName = "deviceFromId")
    RepairOrder toEntity(RepairOrderRequest dto);

    RepairOrderResponse toResponse(RepairOrder entity);

    @Named("customerFromId")
    default Customer customerFromId(Long id) {
        return id == null ? null : Customer.builder().id(id).build();
    }

    @Named("deviceFromId")
    default Device deviceFromId(Long id) {
        return id == null ? null : Device.builder().id(id).build();
    }
}
