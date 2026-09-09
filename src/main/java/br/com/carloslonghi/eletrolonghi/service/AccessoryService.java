package br.com.carloslonghi.eletrolonghi.service;

import br.com.carloslonghi.eletrolonghi.entity.Accessory;
import br.com.carloslonghi.eletrolonghi.exception.EntityInUseException;
import br.com.carloslonghi.eletrolonghi.repository.AccessoryRepository;
import br.com.carloslonghi.eletrolonghi.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AccessoryService {

    private final AccessoryRepository accessoryRepository;
    private final DeviceRepository deviceRepository;

    public List<Accessory> findAll() {
        return accessoryRepository.findAll();
    }

    public Accessory save(Accessory accessory) {
        return accessoryRepository.save(accessory);
    }

    public Optional<Accessory> findById(Long id) {
        return accessoryRepository.findById(id);
    }

    public void deleteById(Long id) {
        if (deviceRepository.existsByAccessoriesId(id)) {
            throw new EntityInUseException("Accessory", id, "aparelho(s)");
        }
        accessoryRepository.deleteById(id);
    }
}
