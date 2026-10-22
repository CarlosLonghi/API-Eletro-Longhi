package br.com.carloslonghi.eletrolonghi.config;

import br.com.carloslonghi.eletrolonghi.entity.enums.RepairOrderStatus;
import br.com.carloslonghi.eletrolonghi.entity.enums.Role;
import br.com.carloslonghi.eletrolonghi.exception.AccountNotActivatedException;
import br.com.carloslonghi.eletrolonghi.exception.DeviceAlreadyInRepairException;
import br.com.carloslonghi.eletrolonghi.exception.EntityInUseException;
import br.com.carloslonghi.eletrolonghi.exception.InvalidPaymentCheckoutException;
import br.com.carloslonghi.eletrolonghi.exception.InvalidRefreshTokenException;
import br.com.carloslonghi.eletrolonghi.exception.InvalidRepairOrderStatusTransitionException;
import br.com.carloslonghi.eletrolonghi.exception.PaymentAlreadyExistsForRepairOrderException;
import br.com.carloslonghi.eletrolonghi.exception.CustomerContactMissingException;
import br.com.carloslonghi.eletrolonghi.exception.PaymentGatewayException;
import br.com.carloslonghi.eletrolonghi.exception.ReceiptDeliveryException;
import br.com.carloslonghi.eletrolonghi.exception.ReferencedEntityNotFoundException;
import br.com.carloslonghi.eletrolonghi.exception.RepairOrderMissingEstimateException;
import br.com.carloslonghi.eletrolonghi.exception.RepairOrderNotApprovedForPaymentException;
import br.com.carloslonghi.eletrolonghi.exception.RepairOrderNotPaidException;
import br.com.carloslonghi.eletrolonghi.exception.RepairOrderStatusActorNotAllowedException;
import br.com.carloslonghi.eletrolonghi.exception.TooManyLoginAttemptsException;
import br.com.carloslonghi.eletrolonghi.exception.UsernameOrPasswordInvalidException;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.lang.reflect.Method;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicationControllerAdviceTest {

    private final ApplicationControllerAdvice advice = new ApplicationControllerAdvice();

    @Test
    void shouldReturnAuthMessage() {
        String message = advice.handleAuthException(new UsernameOrPasswordInvalidException("invalid"));
        assertThat(message).isEqualTo("invalid");
    }

    @Test
    void shouldReturnRefreshTokenMessage() {
        String message = advice.handleInvalidRefreshTokenException(new InvalidRefreshTokenException("bad token"));
        assertThat(message).isEqualTo("bad token");
    }

    @Test
    void shouldReturnAccountNotActivatedMessage() {
        String message = advice.handleAccountNotActivatedException(new AccountNotActivatedException("aguardando ativação"));
        assertThat(message).isEqualTo("aguardando ativação");
    }

    @Test
    void shouldReturnTooManyAttemptsMessage() {
        String message = advice.handleTooManyLoginAttemptsException(new TooManyLoginAttemptsException("blocked"));
        assertThat(message).isEqualTo("blocked");
    }

    @Test
    void shouldReturnDeviceAlreadyInRepairMessage() {
        String message = advice.handleDeviceAlreadyInRepairException(new DeviceAlreadyInRepairException(9L));
        assertThat(message).contains("9");
    }

    @Test
    void shouldReturnInvalidPaymentCheckoutMessage() {
        String message = advice.handleInvalidPaymentCheckoutException(
                new InvalidPaymentCheckoutException("pagamento não é checkout"));
        assertThat(message).isEqualTo("pagamento não é checkout");
    }

    @Test
    void shouldReturnPaymentGatewayMessage() {
        String message = advice.handlePaymentGatewayException(
                new PaymentGatewayException("Mercado Pago indisponível"));
        assertThat(message).isEqualTo("Mercado Pago indisponível");
    }

    @Test
    void shouldReturnCustomerContactMissingMessage() {
        String message = advice.handleCustomerContactMissingException(
                new CustomerContactMissingException("sem e-mail"));
        assertThat(message).isEqualTo("sem e-mail");
    }

    @Test
    void shouldReturnReceiptDeliveryMessage() {
        String message = advice.handleReceiptDeliveryException(
                new ReceiptDeliveryException("SMTP indisponível", new RuntimeException()));
        assertThat(message).isEqualTo("SMTP indisponível");
    }

    @Test
    void shouldReturnReferencedEntityNotFoundMessage() {
        String message = advice.handleReferencedEntityNotFoundException(
                new ReferencedEntityNotFoundException("Brand", 7L));
        assertThat(message).contains("Brand", "7");
    }

    @Test
    void shouldReturnInvalidStatusTransitionMessage() {
        String message = advice.handleInvalidRepairOrderStatusTransitionException(
                new InvalidRepairOrderStatusTransitionException(
                        RepairOrderStatus.AWAITING_EVALUATION, RepairOrderStatus.IN_REPAIR));
        assertThat(message).contains(
                RepairOrderStatus.AWAITING_EVALUATION.getDescription(), RepairOrderStatus.IN_REPAIR.getDescription());
    }

    @Test
    void shouldReturnRepairOrderNotPaidMessage() {
        String message = advice.handleRepairOrderNotPaidException(new RepairOrderNotPaidException(3L));
        assertThat(message).contains("3");
    }

    @Test
    void shouldReturnRepairOrderMissingEstimateMessage() {
        String message = advice.handleRepairOrderMissingEstimateException(new RepairOrderMissingEstimateException(4L));
        assertThat(message).contains("4");
    }

    @Test
    void shouldReturnStatusActorNotAllowedMessage() {
        String message = advice.handleRepairOrderStatusActorNotAllowedException(
                new RepairOrderStatusActorNotAllowedException(5L, Role.TECNICO, RepairOrderStatus.DEVICE_COLLECTED));
        assertThat(message).contains("5", "TECNICO", "DEVICE_COLLECTED");
    }

    @Test
    void shouldReturnPaymentAlreadyExistsMessage() {
        String message = advice.handlePaymentAlreadyExistsForRepairOrderException(
                new PaymentAlreadyExistsForRepairOrderException(6L));
        assertThat(message).contains("6");
    }

    @Test
    void shouldReturnRepairOrderNotApprovedForPaymentMessage() {
        String message = advice.handleRepairOrderNotApprovedForPaymentException(
                new RepairOrderNotApprovedForPaymentException(8L, RepairOrderStatus.IN_EVALUATION));
        assertThat(message).contains("8", RepairOrderStatus.IN_EVALUATION.getDescription());
    }

    @Test
    void shouldReturnRepairOrderNotApprovedForPaymentMessageWhenStatusUnknown() {
        String message = advice.handleRepairOrderNotApprovedForPaymentException(
                new RepairOrderNotApprovedForPaymentException(8L, null));
        assertThat(message).contains("indefinido");
    }

    @Test
    void shouldReturnEntityInUseMessage() {
        String message = advice.handleEntityInUseException(new EntityInUseException("Brand", 2L, "aparelho(s)"));
        assertThat(message).contains("Brand", "2", "aparelho(s)");
    }

    @Test
    void shouldReturnGenericDataIntegrityMessage() {
        String message = advice.handleDataIntegrityViolationException(
                new DataIntegrityViolationException("duplicate key value violates unique constraint \"users_email_key\""));

        assertThat(message).doesNotContain("users_email_key");
        assertThat(message).isNotBlank();
    }

    @Test
    void shouldReturnValidationErrorsMap() throws Exception {
        Method method = TestController.class.getDeclaredMethod("method", String.class);
        MethodParameter methodParameter = new MethodParameter(method, 0);
        BeanPropertyBindingResult result = new BeanPropertyBindingResult(new Object(), "object");
        result.addError(new FieldError("object", "name", "erro"));

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(methodParameter, result);

        Map<String, String> errors = advice.handlerArgumentNotValidException(ex);

        assertThat(errors).isNotEmpty();
    }

    private static class TestController {
        public void method(String value) {
        }
    }
}


