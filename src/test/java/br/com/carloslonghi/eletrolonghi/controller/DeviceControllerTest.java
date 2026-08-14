package br.com.carloslonghi.eletrolonghi.controller;

import br.com.carloslonghi.eletrolonghi.controller.request.DeviceRequest;
import br.com.carloslonghi.eletrolonghi.controller.response.AccessoryResponse;
import br.com.carloslonghi.eletrolonghi.controller.response.BrandResponse;
import br.com.carloslonghi.eletrolonghi.controller.response.DeviceResponse;
import br.com.carloslonghi.eletrolonghi.entity.Device;
import br.com.carloslonghi.eletrolonghi.mapper.DeviceMapper;
import br.com.carloslonghi.eletrolonghi.service.DeviceService;
import br.com.carloslonghi.eletrolonghi.support.TestFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeviceControllerTest {

    @Mock
    private DeviceService deviceService;

    @Mock
    private DeviceMapper deviceMapper;

    @InjectMocks
    private DeviceController deviceController;

    @Test
    void shouldReturnPagedDevices() {
        Device device = TestFixtures.device(1L);
        DeviceResponse response = new DeviceResponse(1L, "M", "S", new BrandResponse(1L, "B"), List.of(new AccessoryResponse(1L, "A")));
        when(deviceService.findAll(any(), any(), any())).thenReturn(new PageImpl<>(List.of(device)));
        when(deviceMapper.toResponse(device)).thenReturn(response);

        var result = deviceController.getAllDevices(null, null, 0, 10, "id", "asc");

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().getContent()).containsExactly(response);
    }

    @Test
    void shouldCreateDevice() {
        DeviceRequest request = new DeviceRequest("M", "S", 1L, List.of(1L));
        Device entity = TestFixtures.device(1L);
        DeviceResponse response = new DeviceResponse(1L, "M", "S", new BrandResponse(1L, "B"), List.of());

        when(deviceMapper.toEntity(request)).thenReturn(entity);
        when(deviceService.save(entity)).thenReturn(entity);
        when(deviceMapper.toResponse(entity)).thenReturn(response);

        var result = deviceController.createDevice(request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getBody()).isEqualTo(response);
    }

    @Test
    void shouldReturnNotFoundWhenDeviceMissingById() {
        when(deviceService.findById(1L)).thenReturn(Optional.empty());

        var result = deviceController.getDeviceById(1L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldReturnDeviceByIdWhenFound() {
        Device entity = TestFixtures.device(1L);
        DeviceResponse response = new DeviceResponse(1L, "M", "S", new BrandResponse(1L, "B"), List.of());
        when(deviceService.findById(1L)).thenReturn(Optional.of(entity));
        when(deviceMapper.toResponse(entity)).thenReturn(response);

        var result = deviceController.getDeviceById(1L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(response);
    }

    @Test
    void shouldReturnDeviceBySerialWhenFound() {
        Device entity = TestFixtures.device(1L);
        DeviceResponse response = new DeviceResponse(1L, "M", "S", new BrandResponse(1L, "B"), List.of());
        when(deviceService.findBySerialNumber("SER")).thenReturn(Optional.of(entity));
        when(deviceMapper.toResponse(entity)).thenReturn(response);

        var result = deviceController.getDeviceBySerialNumber("SER");

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void shouldReturnNotFoundBySerialWhenMissing() {
        when(deviceService.findBySerialNumber("SER")).thenReturn(Optional.empty());

        var result = deviceController.getDeviceBySerialNumber("SER");

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldUpdateWhenFound() {
        DeviceRequest request = new DeviceRequest("M", "S", 1L, List.of(1L));
        Device entity = TestFixtures.device(1L);
        DeviceResponse response = new DeviceResponse(1L, "M", "S", new BrandResponse(1L, "B"), List.of());
        when(deviceMapper.toEntity(request)).thenReturn(entity);
        when(deviceService.update(1L, entity)).thenReturn(Optional.of(entity));
        when(deviceMapper.toResponse(entity)).thenReturn(response);

        var result = deviceController.updateDevice(1L, request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void shouldReturnNotFoundOnUpdateWhenMissing() {
        DeviceRequest request = new DeviceRequest("M", "S", 1L, List.of(1L));
        Device entity = TestFixtures.device(1L);
        when(deviceMapper.toEntity(request)).thenReturn(entity);
        when(deviceService.update(1L, entity)).thenReturn(Optional.empty());

        var result = deviceController.updateDevice(1L, request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldDeleteWhenFound() {
        when(deviceService.findById(1L)).thenReturn(Optional.of(TestFixtures.device(1L)));

        var result = deviceController.deleteDeviceById(1L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void shouldReturnNotFoundOnDeleteWhenMissing() {
        when(deviceService.findById(1L)).thenReturn(Optional.empty());

        var result = deviceController.deleteDeviceById(1L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldReturnSearchByBrand() {
        Device device = TestFixtures.device(1L);
        DeviceResponse response = new DeviceResponse(1L, "M", "S", new BrandResponse(1L, "B"), List.of());
        when(deviceService.findAll(any(), any(), any())).thenReturn(new PageImpl<>(List.of(device)));
        when(deviceMapper.toResponse(device)).thenReturn(response);

        var result = deviceController.getDevicesByBrandId(1L, null, 0, 10, "id", "asc");

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().getContent()).containsExactly(response);
    }
}


