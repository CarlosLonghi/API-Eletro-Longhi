package br.com.carloslonghi.eletrolonghi.service;

import br.com.carloslonghi.eletrolonghi.entity.Accessory;
import br.com.carloslonghi.eletrolonghi.repository.AccessoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AccessoryService {

    private final AccessoryRepository accessoryRepository;

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
        accessoryRepository.deleteById(id);
    }
}
