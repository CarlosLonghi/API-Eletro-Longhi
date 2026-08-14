package br.com.carloslonghi.eletrolonghi.controller;

import br.com.carloslonghi.eletrolonghi.controller.request.AccessoryRequest;
import br.com.carloslonghi.eletrolonghi.controller.response.AccessoryResponse;
import br.com.carloslonghi.eletrolonghi.entity.Accessory;
import br.com.carloslonghi.eletrolonghi.mapper.AccessoryMapper;
import br.com.carloslonghi.eletrolonghi.service.AccessoryService;
import br.com.carloslonghi.eletrolonghi.support.TestFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccessoryControllerTest {

    @Mock
    private AccessoryService accessoryService;

    @Mock
    private AccessoryMapper accessoryMapper;

    @InjectMocks
    private AccessoryController accessoryController;

    @Test
    void shouldReturnAllAccessories() {
        Accessory accessory = TestFixtures.accessory(1L);
        AccessoryResponse response = new AccessoryResponse(1L, "A");
        when(accessoryService.findAll()).thenReturn(List.of(accessory));
        when(accessoryMapper.toResponse(accessory)).thenReturn(response);

        var result = accessoryController.getAllAccessories();

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).containsExactly(response);
    }

    @Test
    void shouldCreateAccessory() {
        AccessoryRequest request = new AccessoryRequest("A");
        Accessory entity = TestFixtures.accessory(1L);
        AccessoryResponse response = new AccessoryResponse(1L, "A");

        when(accessoryMapper.toEntity(request)).thenReturn(entity);
        when(accessoryService.save(entity)).thenReturn(entity);
        when(accessoryMapper.toResponse(entity)).thenReturn(response);

        var result = accessoryController.createAccessory(request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getBody()).isEqualTo(response);
    }

    @Test
    void shouldReturnNotFoundOnDeleteWhenMissing() {
        when(accessoryService.findById(9L)).thenReturn(Optional.empty());

        var result = accessoryController.deleteAccessoryById(9L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldGetAccessoryByIdWhenFound() {
        Accessory accessory = TestFixtures.accessory(1L);
        AccessoryResponse response = new AccessoryResponse(1L, "A");
        when(accessoryService.findById(1L)).thenReturn(Optional.of(accessory));
        when(accessoryMapper.toResponse(accessory)).thenReturn(response);

        var result = accessoryController.getAccessoryById(1L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void shouldDeleteAccessoryWhenFound() {
        when(accessoryService.findById(1L)).thenReturn(Optional.of(TestFixtures.accessory(1L)));

        var result = accessoryController.deleteAccessoryById(1L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }
}

