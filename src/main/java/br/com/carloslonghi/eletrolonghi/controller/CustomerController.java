package br.com.carloslonghi.eletrolonghi.controller;

import br.com.carloslonghi.eletrolonghi.controller.api.spec.CustomerApi;
import br.com.carloslonghi.eletrolonghi.controller.request.CustomerRequest;
import br.com.carloslonghi.eletrolonghi.controller.response.CustomerResponse;
import br.com.carloslonghi.eletrolonghi.entity.Customer;
import br.com.carloslonghi.eletrolonghi.mapper.CustomerMapper;
import br.com.carloslonghi.eletrolonghi.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/customer")
@RequiredArgsConstructor
public class CustomerController implements CustomerApi {

    private final CustomerService customerService;

    @GetMapping
    public ResponseEntity<List<CustomerResponse>> getAllCustomers() {
        List<CustomerResponse> customers = customerService.findAll()
                .stream()
                .map(CustomerMapper::toCustomerResponse)
                .toList();

        return ResponseEntity.ok(customers);
    }

    @PostMapping
    public ResponseEntity<CustomerResponse> createCustomer(@Valid @RequestBody CustomerRequest request) {
        Customer customerEntity = CustomerMapper.toCustomerEntity(request);
        Customer customerCreated = customerService.save(customerEntity);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CustomerMapper.toCustomerResponse(customerCreated));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse> getCustomerById(@PathVariable Long id) {
        return customerService.findById(id)
                .map(customer ->
                        ResponseEntity.ok(CustomerMapper.toCustomerResponse(customer))
                )
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomerResponse> updateCustomer(@PathVariable Long id, @Valid @RequestBody CustomerRequest request) {
        Customer customerEntity = CustomerMapper.toCustomerEntity(request);

        return customerService.update(id, customerEntity)
                .map(customer ->
                    ResponseEntity.ok(CustomerMapper.toCustomerResponse(customer))
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
