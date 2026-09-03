package br.com.carloslonghi.eletrolonghi.mapper;

import br.com.carloslonghi.eletrolonghi.controller.request.PaymentRequest;
import br.com.carloslonghi.eletrolonghi.controller.response.PaymentResponse;
import br.com.carloslonghi.eletrolonghi.entity.Payment;
import br.com.carloslonghi.eletrolonghi.entity.RepairOrder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    @Mapping(target = "repairOrder", source = "repairOrder", qualifiedByName = "repairOrderFromId")
    Payment toEntity(PaymentRequest dto);

    @Mapping(target = "repairOrderId", source = "repairOrder.id")
    PaymentResponse toResponse(Payment entity);

    @Named("repairOrderFromId")
    default RepairOrder repairOrderFromId(Long id) {
        return id == null ? null : RepairOrder.builder().id(id).build();
    }
}
