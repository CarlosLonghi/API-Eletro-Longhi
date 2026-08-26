package br.com.carloslonghi.eletrolonghi.service;

import br.com.carloslonghi.eletrolonghi.entity.Accessory;
import br.com.carloslonghi.eletrolonghi.entity.Brand;
import br.com.carloslonghi.eletrolonghi.entity.Device;
import br.com.carloslonghi.eletrolonghi.exception.ReferencedEntityNotFoundException;
import br.com.carloslonghi.eletrolonghi.repository.DeviceRepository;
import br.com.carloslonghi.eletrolonghi.support.TestFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeviceServiceTest {

    @Mock
    private DeviceRepository deviceRepository;

    @Mock
    private AccessoryService accessoryService;

    @Mock
    private BrandService brandService;

    @InjectMocks
    private DeviceService deviceService;

    @Test
    void shouldFindWithFilters() {
        Page<Device> page = new PageImpl<>(List.of(TestFixtures.device(1L)));
        when(deviceRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(PageRequest.class)))
                .thenReturn(page);

        Page<Device> result = deviceService.findAll("modelo", 1L, PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void shouldSaveDeviceWithResolvedRelations() {
        Device device = TestFixtures.device(1L);
        Brand brand = TestFixtures.brand(1L);
        Accessory accessory = TestFixtures.accessory(1L);

        when(brandService.findById(1L)).thenReturn(Optional.of(brand));
        when(accessoryService.findById(1L)).thenReturn(Optional.of(accessory));
        when(deviceRepository.save(any(Device.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Device saved = deviceService.save(device);

        assertThat(saved.getBrand()).isEqualTo(brand);
        assertThat(saved.getAccessories()).containsExactly(accessory);
    }

    @Test
    void shouldUpdateWhenDeviceExists() {
        Device existing = TestFixtures.device(1L);
        existing.setAccessories(new java.util.ArrayList<>(existing.getAccessories()));
        Device incoming = TestFixtures.device(2L);

        when(deviceRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(brandService.findById(1L)).thenReturn(Optional.of(TestFixtures.brand(1L)));
        when(accessoryService.findById(1L)).thenReturn(Optional.of(TestFixtures.accessory(1L)));
        when(deviceRepository.save(existing)).thenReturn(existing);

        Optional<Device> updated = deviceService.update(1L, incoming);

        assertThat(updated).isPresent();
        assertThat(existing.getModel()).isEqualTo(incoming.getModel());
        verify(deviceRepository).save(existing);
    }

    @Test
    void shouldReturnEmptyWhenUpdatingMissingDevice() {
        when(deviceRepository.findById(1L)).thenReturn(Optional.empty());

        assertThat(deviceService.update(1L, TestFixtures.device(1L))).isEmpty();
    }

    @Test
    void shouldFindByIdAndSerialAndBrandDelegates() {
        Device device = TestFixtures.device(1L);
        when(deviceRepository.findById(1L)).thenReturn(Optional.of(device));
        when(deviceRepository.findBySerialNumber("SERIAL-1")).thenReturn(Optional.of(device));
        when(deviceRepository.findDevicesByBrandId(1L)).thenReturn(List.of(device));

        assertThat(deviceService.findById(1L)).contains(device);
        assertThat(deviceService.findBySerialNumber("SERIAL-1")).contains(device);
        assertThat(deviceService.findDevicesByBrandId(1L)).hasSize(1);
    }

    @Test
    void shouldDeleteById() {
        deviceService.deleteById(5L);

        verify(deviceRepository).deleteById(5L);
    }

    @Test
    void shouldThrowWhenAccessoryMissingOnSave() {
        Device device = TestFixtures.device(1L);
        when(accessoryService.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deviceService.save(device))
                .isInstanceOf(ReferencedEntityNotFoundException.class);
    }

    @Test
    void shouldThrowWhenBrandMissingOnSave() {
        Device device = TestFixtures.device(1L);
        when(accessoryService.findById(1L)).thenReturn(Optional.of(TestFixtures.accessory(1L)));
        when(brandService.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deviceService.save(device))
                .isInstanceOf(ReferencedEntityNotFoundException.class);
    }
}

