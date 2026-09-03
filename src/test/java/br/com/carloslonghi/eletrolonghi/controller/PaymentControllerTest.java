package br.com.carloslonghi.eletrolonghi.controller;

import br.com.carloslonghi.eletrolonghi.client.dto.CheckoutPreference;
import br.com.carloslonghi.eletrolonghi.controller.request.PaymentRequest;
import br.com.carloslonghi.eletrolonghi.controller.request.PaymentStatusUpdateRequest;
import br.com.carloslonghi.eletrolonghi.controller.response.PaymentResponse;
import br.com.carloslonghi.eletrolonghi.entity.Payment;
import br.com.carloslonghi.eletrolonghi.entity.enums.PaymentMethod;
import br.com.carloslonghi.eletrolonghi.entity.enums.PaymentStatus;
import br.com.carloslonghi.eletrolonghi.mapper.PaymentMapper;
import br.com.carloslonghi.eletrolonghi.service.PaymentReceiptService;
import br.com.carloslonghi.eletrolonghi.service.PaymentService;
import br.com.carloslonghi.eletrolonghi.support.TestFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

    @Mock
    private PaymentService paymentService;

    @Mock
    private PaymentReceiptService paymentReceiptService;

    @Mock
    private PaymentMapper paymentMapper;

    @InjectMocks
    private PaymentController paymentController;

    private final PaymentRequest request = new PaymentRequest(
            new BigDecimal("100.00"), PaymentMethod.CASH, 1L, 1, PaymentStatus.PENDING, null, null, null);

    private final PaymentResponse response = PaymentResponse.builder()
            .id(1L).amount(new BigDecimal("100.00")).method(PaymentMethod.CASH)
            .status(PaymentStatus.PENDING).repairOrderId(1L).build();

    @Test
    void shouldReturnPagedPayments() {
        Payment payment = TestFixtures.payment(1L);
        when(paymentService.findAll(any(), any(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(payment)));
        when(paymentMapper.toResponse(payment)).thenReturn(response);

        var result = paymentController.getAllPayments(null, null, null, null, null, 0, 10, "id", "asc");

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().getContent()).containsExactly(response);
    }

    @Test
    void shouldCreatePayment() {
        Payment entity = TestFixtures.payment(1L);
        when(paymentMapper.toEntity(request)).thenReturn(entity);
        when(paymentService.save(entity)).thenReturn(entity);
        when(paymentMapper.toResponse(entity)).thenReturn(response);

        var result = paymentController.createPayment(request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getBody()).isEqualTo(response);
    }

    @Test
    void shouldGetPaymentByIdWhenFound() {
        Payment entity = TestFixtures.payment(1L);
        when(paymentService.findById(1L)).thenReturn(Optional.of(entity));
        when(paymentMapper.toResponse(entity)).thenReturn(response);

        var result = paymentController.getPaymentById(1L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void shouldReturnNotFoundWhenPaymentMissing() {
        when(paymentService.findById(1L)).thenReturn(Optional.empty());

        assertThat(paymentController.getPaymentById(1L).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldUpdatePaymentWhenFound() {
        Payment entity = TestFixtures.payment(1L);
        when(paymentMapper.toEntity(request)).thenReturn(entity);
        when(paymentService.update(1L, entity)).thenReturn(Optional.of(entity));
        when(paymentMapper.toResponse(entity)).thenReturn(response);

        var result = paymentController.updatePayment(1L, request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingMissingPayment() {
        Payment entity = TestFixtures.payment(1L);
        when(paymentMapper.toEntity(request)).thenReturn(entity);
        when(paymentService.update(1L, entity)).thenReturn(Optional.empty());

        assertThat(paymentController.updatePayment(1L, request).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldUpdateStatusWhenFound() {
        Payment entity = TestFixtures.payment(1L);
        when(paymentService.updateStatus(1L, PaymentStatus.APPROVED)).thenReturn(Optional.of(entity));
        when(paymentMapper.toResponse(entity)).thenReturn(response);

        var result = paymentController.updatePaymentStatus(1L, new PaymentStatusUpdateRequest(PaymentStatus.APPROVED));

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingStatusOfMissingPayment() {
        when(paymentService.updateStatus(1L, PaymentStatus.APPROVED)).thenReturn(Optional.empty());

        assertThat(paymentController.updatePaymentStatus(1L, new PaymentStatusUpdateRequest(PaymentStatus.APPROVED))
                .getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldReturnCheckoutLinkWhenFound() {
        when(paymentService.createCheckoutLink(1L))
                .thenReturn(Optional.of(new CheckoutPreference("pref-1", "https://mp/checkout", "https://mp/sandbox")));

        var result = paymentController.createCheckoutLink(1L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().initPoint()).isEqualTo("https://mp/checkout");
    }

    @Test
    void shouldReturnNotFoundWhenCreatingCheckoutLinkForMissingPayment() {
        when(paymentService.createCheckoutLink(1L)).thenReturn(Optional.empty());

        assertThat(paymentController.createCheckoutLink(1L).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldSyncPaymentWhenFound() {
        Payment entity = TestFixtures.payment(1L);
        when(paymentService.syncWithGateway(1L)).thenReturn(Optional.of(entity));
        when(paymentMapper.toResponse(entity)).thenReturn(response);

        var result = paymentController.syncPayment(1L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void shouldReturnNotFoundWhenSyncingMissingPayment() {
        when(paymentService.syncWithGateway(1L)).thenReturn(Optional.empty());

        assertThat(paymentController.syncPayment(1L).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldReturnReceiptPdfWhenFound() {
        Payment entity = TestFixtures.payment(1L);
        byte[] pdf = "%PDF-1.4 fake".getBytes();
        when(paymentService.findById(1L)).thenReturn(Optional.of(entity));
        when(paymentReceiptService.generate(entity)).thenReturn(pdf);

        var result = paymentController.getPaymentReceipt(1L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_PDF);
        assertThat(result.getBody()).isEqualTo(pdf);
    }

    @Test
    void shouldReturnNotFoundForReceiptOfMissingPayment() {
        when(paymentService.findById(1L)).thenReturn(Optional.empty());

        assertThat(paymentController.getPaymentReceipt(1L).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldDeletePaymentWhenFound() {
        when(paymentService.findById(1L)).thenReturn(Optional.of(TestFixtures.payment(1L)));

        assertThat(paymentController.deletePaymentById(1L).getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void shouldReturnNotFoundOnDeleteWhenMissing() {
        when(paymentService.findById(1L)).thenReturn(Optional.empty());

        assertThat(paymentController.deletePaymentById(1L).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
