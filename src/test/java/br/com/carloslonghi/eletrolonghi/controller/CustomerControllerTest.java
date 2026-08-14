package br.com.carloslonghi.eletrolonghi.controller;

import br.com.carloslonghi.eletrolonghi.controller.request.CustomerRequest;
import br.com.carloslonghi.eletrolonghi.controller.response.CustomerResponse;
import br.com.carloslonghi.eletrolonghi.entity.Customer;
import br.com.carloslonghi.eletrolonghi.mapper.CustomerMapper;
import br.com.carloslonghi.eletrolonghi.service.CustomerService;
import br.com.carloslonghi.eletrolonghi.support.TestFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerControllerTest {

    @Mock
    private CustomerService customerService;

    @Mock
    private CustomerMapper customerMapper;

    @InjectMocks
    private CustomerController customerController;

    @Test
    void shouldReturnPagedCustomers() {
        Customer customer = TestFixtures.customer(1L);
        CustomerResponse response = new CustomerResponse(1L, "C", "11", "c@mail.com");
        when(customerService.findAll(any(), any(), any(), any())).thenReturn(new PageImpl<>(List.of(customer)));
        when(customerMapper.toResponse(customer)).thenReturn(response);

        var result = customerController.getAllCustomers(null, null, null, 0, 10, "id", "asc");

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().getContent()).containsExactly(response);
    }

    @Test
    void shouldUpdateCustomerWhenFound() {
        CustomerRequest request = new CustomerRequest("C", "11", "c@mail.com");
        Customer entity = TestFixtures.customer(1L);
        CustomerResponse response = new CustomerResponse(1L, "C", "11", "c@mail.com");

        when(customerMapper.toEntity(request)).thenReturn(entity);
        when(customerService.update(1L, entity)).thenReturn(Optional.of(entity));
        when(customerMapper.toResponse(entity)).thenReturn(response);

        var result = customerController.updateCustomer(1L, request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(response);
    }

    @Test
    void shouldCreateCustomer() {
        CustomerRequest request = new CustomerRequest("C", "11", "c@mail.com");
        Customer entity = TestFixtures.customer(1L);
        CustomerResponse response = new CustomerResponse(1L, "C", "11", "c@mail.com");
        when(customerMapper.toEntity(request)).thenReturn(entity);
        when(customerService.save(entity)).thenReturn(entity);
        when(customerMapper.toResponse(entity)).thenReturn(response);

        var result = customerController.createCustomer(request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getBody()).isEqualTo(response);
    }

    @Test
    void shouldGetCustomerByIdWhenFound() {
        Customer entity = TestFixtures.customer(1L);
        CustomerResponse response = new CustomerResponse(1L, "C", "11", "c@mail.com");
        when(customerService.findById(1L)).thenReturn(Optional.of(entity));
        when(customerMapper.toResponse(entity)).thenReturn(response);

        var result = customerController.getCustomerById(1L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void shouldReturnNotFoundWhenGetCustomerMissing() {
        when(customerService.findById(1L)).thenReturn(Optional.empty());

        var result = customerController.getCustomerById(1L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldDeleteCustomerWhenFound() {
        when(customerService.findById(1L)).thenReturn(Optional.of(TestFixtures.customer(1L)));

        var result = customerController.deleteCustomerById(1L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void shouldReturnNotFoundOnDeleteCustomerWhenMissing() {
        when(customerService.findById(1L)).thenReturn(Optional.empty());

        var result = customerController.deleteCustomerById(1L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}


