package br.com.carloslonghi.eletrolonghi.repository;

import br.com.carloslonghi.eletrolonghi.entity.Customer;
import br.com.carloslonghi.eletrolonghi.repository.specification.CustomerSpecification;
import br.com.carloslonghi.eletrolonghi.repository.support.AbstractPostgresIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class CustomerRepositoryIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private CustomerRepository customerRepository;

    @BeforeEach
    void clean() {
        customerRepository.deleteAll();
    }

    @Test
    void shouldFilterCustomersBySpecification() {
        customerRepository.save(Customer.builder().name("Ana Silva").phone("11999990001").email("ana@mail.com").build());
        customerRepository.save(Customer.builder().name("Carlos Souza").phone("21999990002").email("carlos@mail.com").build());

        assertThat(customerRepository.findAll(CustomerSpecification.withFilters("ana", null, null))).hasSize(1);
        assertThat(customerRepository.findAll(CustomerSpecification.withFilters(null, "mail.com", "21"))).hasSize(1);
        assertThat(customerRepository.findAll(CustomerSpecification.withFilters(null, null, null))).hasSize(2);
    }
}

