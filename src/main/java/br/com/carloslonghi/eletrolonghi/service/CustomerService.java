package br.com.carloslonghi.eletrolonghi.service;

import br.com.carloslonghi.eletrolonghi.entity.Customer;
import br.com.carloslonghi.eletrolonghi.repository.CustomerRepository;
import br.com.carloslonghi.eletrolonghi.repository.specification.CustomerSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;

    public List<Customer> findAll() {
        return customerRepository.findAll();
    }

    public Page<Customer> findAll(String name, String email, String phone, Pageable pageable) {
        return customerRepository.findAll(CustomerSpecification.withFilters(name, email, phone), pageable);
    }

    public Customer save(Customer customer) {
        return customerRepository.save(customer);
    }

    public Optional<Customer> findById(Long id) {
        return customerRepository.findById(id);
    }

    public Optional<Customer> update(Long id, Customer customer) {
        Optional<Customer> optionalCustomer = customerRepository.findById(id);

        if (optionalCustomer.isPresent()) {
            Customer customerToUpdate = optionalCustomer.get();
            customerToUpdate.setName(customer.getName());
            customerToUpdate.setPhone(customer.getPhone());
            customerToUpdate.setEmail(customer.getEmail());

            Customer customerUpdated = customerRepository.save(customerToUpdate);
            return Optional.of(customerUpdated);
        }

        return Optional.empty();
    }

    public void deleteById(Long id) {
        customerRepository.deleteById(id);
    }
}
