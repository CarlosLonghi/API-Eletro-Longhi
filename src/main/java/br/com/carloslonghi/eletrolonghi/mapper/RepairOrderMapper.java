package br.com.carloslonghi.eletrolonghi.mapper;

import br.com.carloslonghi.eletrolonghi.controller.request.RepairOrderRequest;
import br.com.carloslonghi.eletrolonghi.controller.response.CustomerResponse;
import br.com.carloslonghi.eletrolonghi.controller.response.DeviceResponse;
import br.com.carloslonghi.eletrolonghi.controller.response.RepairOrderResponse;
import br.com.carloslonghi.eletrolonghi.entity.Customer;
import br.com.carloslonghi.eletrolonghi.entity.Device;
import br.com.carloslonghi.eletrolonghi.entity.RepairOrder;
import lombok.experimental.UtilityClass;

@UtilityClass
public class RepairOrderMapper {
    public static RepairOrder toRepairOrderEntity(RepairOrderRequest dto) {
        Device device = Device.builder().id(dto.device()).build();
        Customer customer = Customer.builder().id(dto.customer()).build();

        return RepairOrder
                .builder()
                .description(dto.description())
                .status(dto.status())
                .device(device)
                .customer(customer)
                .build();
    }

    public static RepairOrderResponse toRepairOrderResponse(RepairOrder entity) {
        DeviceResponse device = DeviceMapper.toDeviceResponse(entity.getDevice());
        CustomerResponse customer = CustomerMapper.toCustomerResponse(entity.getCustomer());

        return RepairOrderResponse
                .builder()
                .id(entity.getId())
                .description(entity.getDescription())
                .status(entity.getStatus())
                .device(device)
                .customer(customer)
                .build();
    }
}
