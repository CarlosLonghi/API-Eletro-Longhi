package br.com.carloslonghi.eletrolonghi.controller;

import br.com.carloslonghi.eletrolonghi.controller.api.spec.PaymentApi;
import br.com.carloslonghi.eletrolonghi.controller.request.PaymentRequest;
import br.com.carloslonghi.eletrolonghi.controller.request.PaymentStatusUpdateRequest;
import br.com.carloslonghi.eletrolonghi.controller.response.PaymentResponse;
import br.com.carloslonghi.eletrolonghi.controller.support.PaginationUtils;
import br.com.carloslonghi.eletrolonghi.entity.Payment;
import br.com.carloslonghi.eletrolonghi.entity.enums.PaymentMethod;
import br.com.carloslonghi.eletrolonghi.entity.enums.PaymentStatus;
import br.com.carloslonghi.eletrolonghi.mapper.PaymentMapper;
import br.com.carloslonghi.eletrolonghi.service.PaymentReceiptService;
import br.com.carloslonghi.eletrolonghi.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController implements PaymentApi {

    private final PaymentService paymentService;
    private final PaymentReceiptService paymentReceiptService;
    private final PaymentMapper paymentMapper;

    @GetMapping
    public ResponseEntity<Page<PaymentResponse>> getAllPayments(
            @RequestParam(required = false) PaymentStatus status,
            @RequestParam(required = false) PaymentMethod method,
            @RequestParam(required = false) Long repairOrderId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        Pageable pageable = PaginationUtils.createPageable(page, size, sortBy, direction);
        Page<PaymentResponse> payments = paymentService
                .findAll(status, method, repairOrderId, createdFrom, createdTo, pageable)
                .map(paymentMapper::toResponse);

        return ResponseEntity.ok(payments);
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody PaymentRequest request) {
        Payment paymentEntity = paymentMapper.toEntity(request);
        Payment paymentCreated = paymentService.save(paymentEntity);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentMapper.toResponse(paymentCreated));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getPaymentById(@PathVariable Long id) {
        return paymentService.findById(id)
                .map(payment -> ResponseEntity.ok(paymentMapper.toResponse(payment)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<PaymentResponse> updatePayment(@PathVariable Long id, @Valid @RequestBody PaymentRequest request) {
        Payment paymentEntity = paymentMapper.toEntity(request);

        return paymentService.update(id, paymentEntity)
                .map(payment -> ResponseEntity.ok(paymentMapper.toResponse(payment)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<PaymentResponse> updatePaymentStatus(@PathVariable Long id, @Valid @RequestBody PaymentStatusUpdateRequest request) {
        return paymentService.updateStatus(id, request.status())
                .map(payment -> ResponseEntity.ok(paymentMapper.toResponse(payment)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/receipt")
    public ResponseEntity<byte[]> getPaymentReceipt(@PathVariable Long id) {
        return paymentService.findById(id)
                .map(payment -> {
                    byte[] pdf = paymentReceiptService.generate(payment);
                    return ResponseEntity.ok()
                            .contentType(MediaType.APPLICATION_PDF)
                            .header("Content-Disposition", ContentDisposition.inline()
                                    .filename("recibo-" + payment.getId() + ".pdf").build().toString())
                            .body(pdf);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePaymentById(@PathVariable Long id) {
        Optional<Payment> optionalPayment = paymentService.findById(id);

        if (optionalPayment.isPresent()) {
            paymentService.deleteById(id);
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.notFound().build();
    }
}
