package br.com.carloslonghi.eletrolonghi.controller;

import br.com.carloslonghi.eletrolonghi.controller.request.RepairOrderRequest;
import br.com.carloslonghi.eletrolonghi.controller.response.RepairOrderResponse;
import br.com.carloslonghi.eletrolonghi.entity.RepairOrder;
import br.com.carloslonghi.eletrolonghi.mapper.RepairOrderMapper;
import br.com.carloslonghi.eletrolonghi.service.RepairOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/repair-order")
@RequiredArgsConstructor
public class RepairOrderController {

    private final RepairOrderService repairOrderService;

    @GetMapping
    public ResponseEntity<List<RepairOrderResponse>> getAllRepairOrders() {
        List<RepairOrderResponse> repairOrders = repairOrderService.findAll()
                .stream()
                .map(RepairOrderMapper::toRepairOrderResponse)
                .toList();

        return ResponseEntity.ok(repairOrders);
    }

    @PostMapping
    public ResponseEntity<RepairOrderResponse> createRepairOrder(@RequestBody RepairOrderRequest request) {
        RepairOrder repairOrderEntity = RepairOrderMapper.toRepairOrderEntity(request);
        RepairOrder repairOrderCreated = repairOrderService.save(repairOrderEntity);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(RepairOrderMapper.toRepairOrderResponse(repairOrderCreated));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RepairOrderResponse> getRepairOrderById(@PathVariable Long id) {
        return repairOrderService.findById(id)
                .map(repairOrder ->
                        ResponseEntity.ok(RepairOrderMapper.toRepairOrderResponse(repairOrder))
                )
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<RepairOrderResponse> updateRepairOrder(@PathVariable Long id, @RequestBody RepairOrderRequest request) {
        RepairOrder repairOrderEntity = RepairOrderMapper.toRepairOrderEntity(request);

        return repairOrderService.update(id, repairOrderEntity)
                .map(repairOrder ->
                    ResponseEntity.ok(RepairOrderMapper.toRepairOrderResponse(repairOrder))
                )
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRepairOrderById(@PathVariable Long id) {
        Optional<RepairOrder> optionalRepairOrder = repairOrderService.findById(id);

        if (optionalRepairOrder.isPresent()) {
            repairOrderService.deleteById(id);
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.notFound().build();
    }
}
