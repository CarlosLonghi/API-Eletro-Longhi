package br.com.carloslonghi.eletrolonghi.mapper;

import br.com.carloslonghi.eletrolonghi.controller.request.DeviceRequest;
import br.com.carloslonghi.eletrolonghi.controller.response.AccessoryResponse;
import br.com.carloslonghi.eletrolonghi.controller.response.BrandResponse;
import br.com.carloslonghi.eletrolonghi.controller.response.DeviceResponse;
import br.com.carloslonghi.eletrolonghi.entity.Accessory;
import br.com.carloslonghi.eletrolonghi.entity.Brand;
import br.com.carloslonghi.eletrolonghi.entity.Device;
import lombok.experimental.UtilityClass;

import java.util.List;

@UtilityClass
public class DeviceMapper {

    public static Device toDeviceEntity(DeviceRequest request) {
        List<Accessory> accessories = request.accessories()
                .stream()
                .map(accessoryId -> Accessory.builder()
                        .id(accessoryId).build()
                )
                .toList();

        Brand brand = Brand.builder().id(request.brand()).build();

        return Device
                .builder()
                .model(request.model())
                .serialNumber(request.serialNumber())
                .accessories(accessories)
                .brand(brand)
                .build();
    }

    public static DeviceResponse toDeviceResponse(Device response) {
        List<AccessoryResponse> accessories = response.getAccessories()
                .stream()
                .map(AccessoryMapper::toAccessoryResponse)
                .toList();

        BrandResponse brand = BrandMapper.toBrandResponse(response.getBrand());

        return DeviceResponse
                .builder()
                .id(response.getId())
                .model(response.getModel())
                .serialNumber(response.getSerialNumber())
                .accessories(accessories)
                .brand(brand)
                .build();
    }
}
