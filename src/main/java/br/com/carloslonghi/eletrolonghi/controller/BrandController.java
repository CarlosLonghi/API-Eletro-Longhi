package br.com.carloslonghi.eletrolonghi.controller;

import br.com.carloslonghi.eletrolonghi.controller.api.spec.BrandApi;
import br.com.carloslonghi.eletrolonghi.controller.request.BrandRequest;
import br.com.carloslonghi.eletrolonghi.controller.response.BrandResponse;
import br.com.carloslonghi.eletrolonghi.entity.Brand;
import br.com.carloslonghi.eletrolonghi.mapper.BrandMapper;
import br.com.carloslonghi.eletrolonghi.service.BrandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/brand")
@RequiredArgsConstructor
public class BrandController implements BrandApi {

    private final BrandService brandService;

    @GetMapping
    public ResponseEntity<List<BrandResponse>> getAllBrands() {
        List<BrandResponse> brands = brandService.findAll()
                .stream()
                .map(BrandMapper::toBrandResponse)
                .toList();

        return ResponseEntity.ok(brands);
    }

    @PostMapping
    public ResponseEntity<BrandResponse> createBrand(@Valid @RequestBody BrandRequest request) {
        Brand brandEntity = BrandMapper.toBrandEntity(request);
        Brand createdBrand = brandService.save(brandEntity);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BrandMapper.toBrandResponse(createdBrand));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BrandResponse> getBrandById(@PathVariable Long id) {
        return brandService.findById(id)
                .map(brand ->
                        ResponseEntity.ok(BrandMapper.toBrandResponse(brand))
                )
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBrandById(@PathVariable Long id) {
        Optional<Brand> optionalBrand = brandService.findById(id);

        if (optionalBrand.isPresent()) {
            brandService.deleteById(id);
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.notFound().build();
    }
}
