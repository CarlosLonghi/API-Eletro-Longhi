package br.com.carloslonghi.eletrolonghi.service;

import br.com.carloslonghi.eletrolonghi.entity.Customer;
import br.com.carloslonghi.eletrolonghi.repository.CustomerRepository;
import br.com.carloslonghi.eletrolonghi.support.TestFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    @Test
    void shouldFindCustomersWithFilters() {
        Page<Customer> page = new PageImpl<>(List.of(TestFixtures.customer(1L)));
        when(customerRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(PageRequest.class)))
                .thenReturn(page);

        Page<Customer> result = customerService.findAll("ana", "ana@mail.com", "11", PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void shouldUpdateCustomerWhenFound() {
        Customer existing = TestFixtures.customer(1L);
        Customer incoming = TestFixtures.customer(2L);
        incoming.setName("Novo Nome");
        when(customerRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(customerRepository.save(existing)).thenReturn(existing);

        Optional<Customer> result = customerService.update(1L, incoming);

        assertThat(result).isPresent();
        assertThat(existing.getName()).isEqualTo("Novo Nome");
        verify(customerRepository).save(existing);
    }

    @Test
    void shouldReturnEmptyWhenUpdatingMissingCustomer() {
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<Customer> result = customerService.update(1L, TestFixtures.customer(1L));

        assertThat(result).isEmpty();
    }

    @Test
    void shouldSaveFindAndDeleteCustomer() {
        Customer customer = TestFixtures.customer(1L);
        when(customerRepository.save(customer)).thenReturn(customer);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        assertThat(customerService.save(customer)).isEqualTo(customer);
        assertThat(customerService.findById(1L)).contains(customer);

        customerService.deleteById(1L);
        verify(customerRepository).deleteById(1L);
    }
}

