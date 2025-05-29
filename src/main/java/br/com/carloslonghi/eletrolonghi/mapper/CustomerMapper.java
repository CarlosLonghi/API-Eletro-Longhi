package br.com.carloslonghi.eletrolonghi.mapper;

import br.com.carloslonghi.eletrolonghi.controller.request.CustomerRequest;
import br.com.carloslonghi.eletrolonghi.controller.response.CustomerResponse;
import br.com.carloslonghi.eletrolonghi.entity.Customer;
import lombok.experimental.UtilityClass;

@UtilityClass
public class CustomerMapper {

    public static Customer toCustomerEntity(CustomerRequest dto) {
        return Customer
                .builder()
                .name(dto.name())
                .phone(dto.phone())
                .email(dto.email())
                .build();
    }

    public static CustomerResponse toCustomerResponse(Customer entity) {
        return CustomerResponse
                .builder()
                .id(entity.getId())
                .name(entity.getName())
                .phone(entity.getPhone())
                .email(entity.getEmail())
                .build();
    }
}
