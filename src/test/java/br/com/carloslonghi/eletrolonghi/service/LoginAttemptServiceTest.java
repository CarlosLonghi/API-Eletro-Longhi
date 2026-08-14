package br.com.carloslonghi.eletrolonghi.service;

import br.com.carloslonghi.eletrolonghi.exception.TooManyLoginAttemptsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LoginAttemptServiceTest {

    private LoginAttemptService loginAttemptService;

    @BeforeEach
    void setUp() {
        loginAttemptService = new LoginAttemptService();
        ReflectionTestUtils.setField(loginAttemptService, "maxAttempts", 2);
        ReflectionTestUtils.setField(loginAttemptService, "blockDurationMs", 60_000L);
    }

    @Test
    void shouldBlockAfterMaxAttempts() {
        loginAttemptService.loginFailed("USER@mail.com");
        loginAttemptService.loginFailed("user@mail.com");

        assertThatThrownBy(() -> loginAttemptService.checkBlocked("user@mail.com"))
                .isInstanceOf(TooManyLoginAttemptsException.class);
    }

    @Test
    void shouldRemoveAttemptsOnSuccess() {
        loginAttemptService.loginFailed("user@mail.com");
        loginAttemptService.loginSucceeded("user@mail.com");

        assertThatCode(() -> loginAttemptService.checkBlocked("user@mail.com")).doesNotThrowAnyException();
    }
}
