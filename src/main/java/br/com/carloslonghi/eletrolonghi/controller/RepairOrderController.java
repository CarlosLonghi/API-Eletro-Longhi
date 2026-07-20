package br.com.carloslonghi.eletrolonghi.controller;

import br.com.carloslonghi.eletrolonghi.controller.api.spec.RepairOrderApi;
import br.com.carloslonghi.eletrolonghi.controller.request.RepairOrderRequest;
import br.com.carloslonghi.eletrolonghi.controller.response.RepairOrderResponse;
import br.com.carloslonghi.eletrolonghi.controller.support.PaginationUtils;
import br.com.carloslonghi.eletrolonghi.entity.RepairOrder;
import br.com.carloslonghi.eletrolonghi.entity.enums.RepairOrderStatus;
import br.com.carloslonghi.eletrolonghi.mapper.RepairOrderMapper;
import br.com.carloslonghi.eletrolonghi.service.RepairOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/repair-order")
@RequiredArgsConstructor
public class RepairOrderController implements RepairOrderApi {

    private final RepairOrderService repairOrderService;
    private final RepairOrderMapper repairOrderMapper;

    @GetMapping
    public ResponseEntity<Page<RepairOrderResponse>> getAllRepairOrders(
            @RequestParam(required = false) RepairOrderStatus status,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) Long deviceId,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        Pageable pageable = PaginationUtils.createPageable(page, size, sortBy, direction);
        Page<RepairOrderResponse> repairOrders = repairOrderService.findAll(
                        status,
                        customerId,
                        deviceId,
                        description,
                        createdFrom,
                        createdTo,
                        pageable
                )
                .map(repairOrderMapper::toResponse);

        return ResponseEntity.ok(repairOrders);
    }

    @PostMapping
    public ResponseEntity<RepairOrderResponse> createRepairOrder(@Valid @RequestBody RepairOrderRequest request) {
        RepairOrder repairOrderEntity = repairOrderMapper.toEntity(request);
        RepairOrder repairOrderCreated = repairOrderService.save(repairOrderEntity);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(repairOrderMapper.toResponse(repairOrderCreated));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RepairOrderResponse> getRepairOrderById(@PathVariable Long id) {
        return repairOrderService.findById(id)
                .map(repairOrder ->
                        ResponseEntity.ok(repairOrderMapper.toResponse(repairOrder))
                )
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<RepairOrderResponse> updateRepairOrder(@PathVariable Long id, @Valid @RequestBody RepairOrderRequest request) {
        RepairOrder repairOrderEntity = repairOrderMapper.toEntity(request);

        return repairOrderService.update(id, repairOrderEntity)
                .map(repairOrder ->
                    ResponseEntity.ok(repairOrderMapper.toResponse(repairOrder))
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
