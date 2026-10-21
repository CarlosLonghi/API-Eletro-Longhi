package br.com.carloslonghi.eletrolonghi.service;

import br.com.carloslonghi.eletrolonghi.exception.TooManyLoginAttemptsException;
import br.com.carloslonghi.eletrolonghi.metrics.AuthMetrics;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LoginAttemptServiceTest {

    private LoginAttemptService loginAttemptService;
    private SimpleMeterRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new SimpleMeterRegistry();
        loginAttemptService = new LoginAttemptService(new AuthMetrics(registry));
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

    @Test
    void shouldCountLoginsByResult() {
        loginAttemptService.loginFailed("user@mail.com");
        loginAttemptService.loginFailed("user@mail.com");
        assertThatThrownBy(() -> loginAttemptService.checkBlocked("user@mail.com"))
                .isInstanceOf(TooManyLoginAttemptsException.class);
        loginAttemptService.loginSucceeded("other@mail.com");

        assertThat(logins("failure")).isEqualTo(2);
        assertThat(logins("blocked")).isEqualTo(1);
        assertThat(logins("success")).isEqualTo(1);
    }

    private double logins(String result) {
        return registry.get("eletrolonghi.auth.logins").tag("result", result).counter().count();
    }
}
