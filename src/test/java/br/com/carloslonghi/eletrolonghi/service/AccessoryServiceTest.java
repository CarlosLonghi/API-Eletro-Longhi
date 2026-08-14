package br.com.carloslonghi.eletrolonghi.service;

import br.com.carloslonghi.eletrolonghi.entity.Accessory;
import br.com.carloslonghi.eletrolonghi.repository.AccessoryRepository;
import br.com.carloslonghi.eletrolonghi.support.TestFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccessoryServiceTest {

    @Mock
    private AccessoryRepository accessoryRepository;

    @InjectMocks
    private AccessoryService accessoryService;

    @Test
    void shouldFindAllAccessories() {
        when(accessoryRepository.findAll()).thenReturn(List.of(TestFixtures.accessory(1L)));

        List<Accessory> result = accessoryService.findAll();

        assertThat(result).hasSize(1);
        verify(accessoryRepository).findAll();
    }

    @Test
    void shouldSaveAccessory() {
        Accessory accessory = TestFixtures.accessory(1L);
        when(accessoryRepository.save(accessory)).thenReturn(accessory);

        Accessory result = accessoryService.save(accessory);

        assertThat(result.getId()).isEqualTo(1L);
        verify(accessoryRepository).save(accessory);
    }

    @Test
    void shouldFindAccessoryById() {
        Accessory accessory = TestFixtures.accessory(1L);
        when(accessoryRepository.findById(1L)).thenReturn(Optional.of(accessory));

        Optional<Accessory> result = accessoryService.findById(1L);

        assertThat(result).contains(accessory);
    }

    @Test
    void shouldDeleteAccessoryById() {
        accessoryService.deleteById(10L);

        verify(accessoryRepository).deleteById(10L);
    }
}
