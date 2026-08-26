package br.com.carloslonghi.eletrolonghi.service;

import br.com.carloslonghi.eletrolonghi.entity.Accessory;
import br.com.carloslonghi.eletrolonghi.entity.Brand;
import br.com.carloslonghi.eletrolonghi.entity.Device;
import br.com.carloslonghi.eletrolonghi.exception.ReferencedEntityNotFoundException;
import br.com.carloslonghi.eletrolonghi.repository.DeviceRepository;
import br.com.carloslonghi.eletrolonghi.repository.specification.DeviceSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DeviceService {

    private final DeviceRepository deviceRepository;

    private final AccessoryService accessoryService;
    private final BrandService brandService;

    public List<Device> findAll() {
        return deviceRepository.findAll();
    }

    public Page<Device> findAll(
            String model,
            Long brandId,
            Pageable pageable
    ) {
        return deviceRepository.findAll(
                DeviceSpecification.withFilters(model, brandId),
                pageable
        );
    }

    public Device save(Device device) {
        List<Accessory> accessories = this.findAccessories(device.getAccessories());
        device.setAccessories(accessories);

        Brand brand = this.findBrand(device.getBrand());
        device.setBrand(brand);

        return deviceRepository.save(device);
    }

    public Optional<Device> findById(Long id) {
        return deviceRepository.findById(id);
    }

    public List<Device> findDevicesByBrandId(Long brandId) {
        return deviceRepository.findDevicesByBrandId(brandId);
    }

    public Optional<Device> findBySerialNumber(String serialNumber) {
        return deviceRepository.findBySerialNumber(serialNumber);
    }

    public Optional<Device> update(Long id, Device device) {
        Optional<Device> optionalDevice = deviceRepository.findById(id);

        if (optionalDevice.isPresent()) {
            Brand brand = this.findBrand(device.getBrand());
            List<Accessory> accessories = this.findAccessories(device.getAccessories());

            Device deviceToUpdate = optionalDevice.get();
            deviceToUpdate.setModel(device.getModel());
            deviceToUpdate.setSerialNumber(device.getSerialNumber());
            deviceToUpdate.setBrand(brand);

            deviceToUpdate.getAccessories().clear();
            deviceToUpdate.getAccessories().addAll(accessories);

            Device deviceUpdated = deviceRepository.save(deviceToUpdate);
            return Optional.of(deviceUpdated);
        }

        return Optional.empty();
    }

    public void deleteById(Long id) {
        deviceRepository.deleteById(id);
    }

    private List<Accessory> findAccessories(List<Accessory> accessories) {
        return accessories.stream()
                .map(accessory -> accessoryService.findById(accessory.getId())
                        .orElseThrow(() -> new ReferencedEntityNotFoundException("Accessory", accessory.getId())))
                .toList();
    }

    private Brand findBrand(Brand brand) {
        return brandService.findById(brand.getId())
                .orElseThrow(() -> new ReferencedEntityNotFoundException("Brand", brand.getId()));
    }
}
