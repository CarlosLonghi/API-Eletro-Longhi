package br.com.carloslonghi.eletrolonghi.controller;

import br.com.carloslonghi.eletrolonghi.controller.api.spec.CustomerApi;
import br.com.carloslonghi.eletrolonghi.controller.request.CustomerRequest;
import br.com.carloslonghi.eletrolonghi.controller.response.CustomerResponse;
import br.com.carloslonghi.eletrolonghi.controller.support.PaginationUtils;
import br.com.carloslonghi.eletrolonghi.entity.Customer;
import br.com.carloslonghi.eletrolonghi.mapper.CustomerMapper;
import br.com.carloslonghi.eletrolonghi.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/customer")
@RequiredArgsConstructor
public class CustomerController implements CustomerApi {

    private final CustomerService customerService;
    private final CustomerMapper customerMapper;

    @GetMapping
    public ResponseEntity<Page<CustomerResponse>> getAllCustomers(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phone,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        Pageable pageable = PaginationUtils.createPageable(page, size, sortBy, direction);
        Page<CustomerResponse> customers = customerService.findAll(name, email, phone, pageable)
                .map(customerMapper::toResponse);

        return ResponseEntity.ok(customers);
    }

    @PostMapping
    public ResponseEntity<CustomerResponse> createCustomer(@Valid @RequestBody CustomerRequest request) {
        Customer customerEntity = customerMapper.toEntity(request);
        Customer customerCreated = customerService.save(customerEntity);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(customerMapper.toResponse(customerCreated));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse> getCustomerById(@PathVariable Long id) {
        return customerService.findById(id)
                .map(customer ->
                        ResponseEntity.ok(customerMapper.toResponse(customer))
                )
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomerResponse> updateCustomer(@PathVariable Long id, @Valid @RequestBody CustomerRequest request) {
        Customer customerEntity = customerMapper.toEntity(request);

        return customerService.update(id, customerEntity)
                .map(customer ->
                    ResponseEntity.ok(customerMapper.toResponse(customer))
                )
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomerById(@PathVariable Long id) {
        Optional<Customer> optionalCustomer = customerService.findById(id);

        if (optionalCustomer.isPresent()) {
            customerService.deleteById(id);
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.notFound().build();
    }
}
