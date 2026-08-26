package br.com.carloslonghi.eletrolonghi.config;

import br.com.carloslonghi.eletrolonghi.exception.AccountNotActivatedException;
import br.com.carloslonghi.eletrolonghi.exception.DeviceAlreadyInRepairException;
import br.com.carloslonghi.eletrolonghi.exception.InvalidRefreshTokenException;
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


