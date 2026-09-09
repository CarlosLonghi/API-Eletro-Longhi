package br.com.carloslonghi.eletrolonghi.service;

import br.com.carloslonghi.eletrolonghi.entity.Brand;
import br.com.carloslonghi.eletrolonghi.exception.EntityInUseException;
import br.com.carloslonghi.eletrolonghi.repository.BrandRepository;
import br.com.carloslonghi.eletrolonghi.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BrandService {

    private final BrandRepository brandRepository;
    private final DeviceRepository deviceRepository;

    public List<Brand> findAll() {
        return brandRepository.findAll();
    }

    public Brand save(Brand brand) {
        return brandRepository.save(brand);
    }

    public Optional<Brand> findById(Long id) {
        return brandRepository.findById(id);
    }

    public void deleteById(Long id) {
        if (deviceRepository.existsByBrandId(id)) {
            throw new EntityInUseException("Brand", id, "aparelho(s)");
        }
        brandRepository.deleteById(id);
    }
}
