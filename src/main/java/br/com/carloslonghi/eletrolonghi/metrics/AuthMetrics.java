package br.com.carloslonghi.eletrolonghi.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

/**
 * Tentativas de login, expostas em /actuator/prometheus como
 * eletrolonghi_auth_logins_total{result="success|failure|blocked"}: "blocked" é a tentativa barrada
 * pela proteção contra força bruta antes de conferir a senha. Registradas zeradas na criação,
 * pelo mesmo motivo de {@link RepairOrderMetrics}.
 */
@Component
public class AuthMetrics {

    private final Counter success;
    private final Counter failure;
    private final Counter blocked;

    public AuthMetrics(MeterRegistry registry) {
        success = login(registry, "success");
        failure = login(registry, "failure");
        blocked = login(registry, "blocked");
    }

    private static Counter login(MeterRegistry registry, String result) {
        return Counter.builder("eletrolonghi.auth.logins")
                .description("Tentativas de login, pelo resultado")
                .tag("result", result)
                .register(registry);
    }

    public void success() {
        success.increment();
    }

    public void failure() {
        failure.increment();
    }

    public void blocked() {
        blocked.increment();
    }
}
