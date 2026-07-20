package br.com.carloslonghi.eletrolonghi.controller;

import br.com.carloslonghi.eletrolonghi.controller.api.spec.DeviceApi;
import br.com.carloslonghi.eletrolonghi.controller.request.DeviceRequest;
import br.com.carloslonghi.eletrolonghi.controller.response.DeviceResponse;
import br.com.carloslonghi.eletrolonghi.controller.support.PaginationUtils;
import br.com.carloslonghi.eletrolonghi.entity.Device;
import br.com.carloslonghi.eletrolonghi.mapper.DeviceMapper;
import br.com.carloslonghi.eletrolonghi.service.DeviceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/device")
@RequiredArgsConstructor
public class DeviceController implements DeviceApi {

    private final DeviceService deviceService;
    private final DeviceMapper deviceMapper;

    @GetMapping
    public ResponseEntity<Page<DeviceResponse>> getAllDevices(
            @RequestParam(required = false) String model,
            @RequestParam(required = false) String serialNumber,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) Long accessoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        Pageable pageable = PaginationUtils.createPageable(page, size, sortBy, direction);
        Page<DeviceResponse> devices = deviceService.findAll(model, serialNumber, brandId, accessoryId, pageable)
                .map(deviceMapper::toResponse);

        return ResponseEntity.ok(devices);
    }

    @PostMapping
    public ResponseEntity<DeviceResponse> createDevice(@Valid @RequestBody DeviceRequest request) {
        Device deviceEntity = deviceMapper.toEntity(request);
        Device createdDevice = deviceService.save(deviceEntity);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(deviceMapper.toResponse(createdDevice));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeviceResponse> getDeviceById(@PathVariable Long id) {
        return deviceService.findById(id)
                .map(device ->
                        ResponseEntity.ok(deviceMapper.toResponse(device))
                )
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<DeviceResponse> updateDevice(@PathVariable Long id, @Valid @RequestBody DeviceRequest request) {
        Device deviceEntity = deviceMapper.toEntity(request);

        return deviceService.update(id, deviceEntity)
                .map(device ->
                        ResponseEntity.ok(deviceMapper.toResponse(device))
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
    public ResponseEntity<Page<DeviceResponse>> getDevicesByBrandId(
            @RequestParam Long brandId,
            @RequestParam(required = false) String model,
            @RequestParam(required = false) String serialNumber,
            @RequestParam(required = false) Long accessoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        Pageable pageable = PaginationUtils.createPageable(page, size, sortBy, direction);
        Page<DeviceResponse> devices = deviceService.findAll(model, serialNumber, brandId, accessoryId, pageable)
                .map(deviceMapper::toResponse);

        return ResponseEntity.ok(devices);
    }
}
