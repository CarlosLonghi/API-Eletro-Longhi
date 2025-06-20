package br.com.carloslonghi.eletrolonghi.controller;

import br.com.carloslonghi.eletrolonghi.controller.api.spec.DeviceApi;
import br.com.carloslonghi.eletrolonghi.controller.request.DeviceRequest;
import br.com.carloslonghi.eletrolonghi.controller.response.DeviceResponse;
import br.com.carloslonghi.eletrolonghi.entity.Device;
import br.com.carloslonghi.eletrolonghi.mapper.DeviceMapper;
import br.com.carloslonghi.eletrolonghi.service.DeviceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/device")
@RequiredArgsConstructor
public class DeviceController implements DeviceApi {

    private final DeviceService deviceService;

    @GetMapping
    public ResponseEntity<List<DeviceResponse>> getAllDevices() {
        List<DeviceResponse> devices = deviceService.findAll()
                .stream()
                .map(DeviceMapper::toDeviceResponse)
                .toList();

        return ResponseEntity.ok(devices);
    }

    @PostMapping
    public ResponseEntity<DeviceResponse> createDevice(@Valid @RequestBody DeviceRequest request) {
        Device deviceEntity = DeviceMapper.toDeviceEntity(request);
        Device createdDevice = deviceService.save(deviceEntity);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(DeviceMapper.toDeviceResponse(createdDevice));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeviceResponse> getDeviceById(@PathVariable Long id) {
        return deviceService.findById(id)
                .map(device ->
                        ResponseEntity.ok(DeviceMapper.toDeviceResponse(device))
                )
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<DeviceResponse> updateDevice(@PathVariable Long id, @Valid @RequestBody DeviceRequest request) {
        Device deviceEntity = DeviceMapper.toDeviceEntity(request);

        return deviceService.update(id, deviceEntity)
                .map(device ->
                        ResponseEntity.ok(DeviceMapper.toDeviceResponse(device))
                )
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDeviceById(@PathVariable Long id) {
        Optional<Device> optionalDevice = deviceService.findById(id);

        if (optionalDevice.isPresent()) {
            deviceService.deleteById(id);
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.notFound().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<DeviceResponse>> getDevicesByBrandId(@RequestParam Long brandId) {
        List<DeviceResponse> devices = deviceService.findDevicesByBrandId(brandId)
                .stream()
                .map(DeviceMapper::toDeviceResponse)
                .toList();

        return ResponseEntity.ok(devices);
    }
}
