package br.com.carloslonghi.eletrolonghi.controller;

import br.com.carloslonghi.eletrolonghi.controller.api.spec.AccessoryApi;
import br.com.carloslonghi.eletrolonghi.controller.request.AccessoryRequest;
import br.com.carloslonghi.eletrolonghi.controller.response.AccessoryResponse;
import br.com.carloslonghi.eletrolonghi.entity.Accessory;
import br.com.carloslonghi.eletrolonghi.mapper.AccessoryMapper;
import br.com.carloslonghi.eletrolonghi.service.AccessoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/accessory")
@RequiredArgsConstructor
public class AccessoryController implements AccessoryApi {

    private final AccessoryService accessoryService;

    @GetMapping
    public ResponseEntity<List<AccessoryResponse>> getAllAccessories() {
        List<AccessoryResponse> accessories = accessoryService.findAll()
                .stream()
                .map(AccessoryMapper::toAccessoryResponse)
                .toList();

        return ResponseEntity.ok(accessories);
    }

    @PostMapping
    public ResponseEntity<AccessoryResponse> createAccessory(@Valid @RequestBody AccessoryRequest request) {
        Accessory accessoryEntity = AccessoryMapper.toAccessoryEntity(request);
        Accessory createdAccessory = accessoryService.save(accessoryEntity);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(AccessoryMapper.toAccessoryResponse(createdAccessory));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccessoryResponse> getAccessoryById(@PathVariable Long id) {
        return accessoryService.findById(id)
                .map(accessory ->
                        ResponseEntity.ok(AccessoryMapper.toAccessoryResponse(accessory))
                )
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAccessoryById(@PathVariable Long id) {
        Optional<Accessory> optionalAccessory = accessoryService.findById(id);

        if (optionalAccessory.isPresent()) {
            accessoryService.deleteById(id);
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.notFound().build();
    }
}
