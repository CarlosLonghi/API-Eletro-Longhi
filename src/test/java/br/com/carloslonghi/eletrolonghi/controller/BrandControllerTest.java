package br.com.carloslonghi.eletrolonghi.controller;

import br.com.carloslonghi.eletrolonghi.controller.request.BrandRequest;
import br.com.carloslonghi.eletrolonghi.controller.response.BrandResponse;
import br.com.carloslonghi.eletrolonghi.entity.Brand;
import br.com.carloslonghi.eletrolonghi.mapper.BrandMapper;
import br.com.carloslonghi.eletrolonghi.service.BrandService;
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
class BrandControllerTest {

    @Mock
    private BrandService brandService;

    @Mock
    private BrandMapper brandMapper;

    @InjectMocks
    private BrandController brandController;

    @Test
    void shouldReturnAllBrands() {
        Brand brand = TestFixtures.brand(1L);
        BrandResponse response = new BrandResponse(1L, "B");
        when(brandService.findAll()).thenReturn(List.of(brand));
        when(brandMapper.toResponse(brand)).thenReturn(response);

        var result = brandController.getAllBrands();

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).containsExactly(response);
    }

    @Test
    void shouldCreateBrand() {
        BrandRequest request = new BrandRequest("B");
        Brand entity = TestFixtures.brand(1L);
        BrandResponse response = new BrandResponse(1L, "B");

        when(brandMapper.toEntity(request)).thenReturn(entity);
        when(brandService.save(entity)).thenReturn(entity);
        when(brandMapper.toResponse(entity)).thenReturn(response);

        var result = brandController.createBrand(request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getBody()).isEqualTo(response);
    }

    @Test
    void shouldReturnNotFoundOnDeleteWhenMissing() {
        when(brandService.findById(9L)).thenReturn(Optional.empty());

        var result = brandController.deleteBrandById(9L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldGetBrandByIdWhenFound() {
        Brand brand = TestFixtures.brand(1L);
        BrandResponse response = new BrandResponse(1L, "B");
        when(brandService.findById(1L)).thenReturn(Optional.of(brand));
        when(brandMapper.toResponse(brand)).thenReturn(response);

        var result = brandController.getBrandById(1L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void shouldDeleteBrandWhenFound() {
        when(brandService.findById(1L)).thenReturn(Optional.of(TestFixtures.brand(1L)));

        var result = brandController.deleteBrandById(1L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }
}

