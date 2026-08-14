package br.com.carloslonghi.eletrolonghi.service;

import br.com.carloslonghi.eletrolonghi.entity.Brand;
import br.com.carloslonghi.eletrolonghi.repository.BrandRepository;
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
class BrandServiceTest {

    @Mock
    private BrandRepository brandRepository;

    @InjectMocks
    private BrandService brandService;

    @Test
    void shouldFindAllBrands() {
        when(brandRepository.findAll()).thenReturn(List.of(TestFixtures.brand(1L)));

        List<Brand> result = brandService.findAll();

        assertThat(result).hasSize(1);
        verify(brandRepository).findAll();
    }

    @Test
    void shouldSaveBrand() {
        Brand brand = TestFixtures.brand(1L);
        when(brandRepository.save(brand)).thenReturn(brand);

        Brand result = brandService.save(brand);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void shouldFindBrandById() {
        Brand brand = TestFixtures.brand(1L);
        when(brandRepository.findById(1L)).thenReturn(Optional.of(brand));

        assertThat(brandService.findById(1L)).contains(brand);
    }

    @Test
    void shouldDeleteBrandById() {
        brandService.deleteById(5L);

        verify(brandRepository).deleteById(5L);
    }
}
