package br.com.carloslonghi.eletrolonghi.metrics;

import br.com.carloslonghi.eletrolonghi.entity.enums.PaymentMethod;
import br.com.carloslonghi.eletrolonghi.entity.enums.PaymentStatus;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentMetricsTest {

    private final SimpleMeterRegistry registry = new SimpleMeterRegistry();
    private final PaymentMetrics metrics = new PaymentMetrics(registry);

    @Test
    void shouldRegisterEveryMethodAndStatusZeroed() {
        for (PaymentMethod method : PaymentMethod.values()) {
            assertThat(registry.get("eletrolonghi.payments.registered").tag("method", method.name())
                    .counter().count()).isZero();
            assertThat(registry.get("eletrolonghi.payments.approved.amount").tag("method", method.name())
                    .summary().count()).isZero();
        }
        for (PaymentStatus status : PaymentStatus.values()) {
            assertThat(registry.get("eletrolonghi.payments.status_changes").tag("status", status.name())
                    .counter().count()).isZero();
        }
        assertThat(registry.get("eletrolonghi.payments.checkout_links").counter().count()).isZero();
    }

    @Test
    void shouldRecordEventsByTag() {
        metrics.registered(PaymentMethod.PIX);
        metrics.statusChanged(PaymentStatus.APPROVED);
        metrics.approved(PaymentMethod.PIX, new BigDecimal("150.50"));
        metrics.approved(PaymentMethod.PIX, new BigDecimal("49.50"));
        metrics.checkoutLinkCreated();

        assertThat(registry.get("eletrolonghi.payments.registered").tag("method", "PIX").counter().count()).isEqualTo(1);
        assertThat(registry.get("eletrolonghi.payments.status_changes").tag("status", "APPROVED")
                .counter().count()).isEqualTo(1);
        DistributionSummary approved = registry.get("eletrolonghi.payments.approved.amount").tag("method", "PIX").summary();
        assertThat(approved.count()).isEqualTo(2);
        assertThat(approved.totalAmount()).isEqualTo(200.0);
        assertThat(registry.get("eletrolonghi.payments.checkout_links").counter().count()).isEqualTo(1);
    }
}
