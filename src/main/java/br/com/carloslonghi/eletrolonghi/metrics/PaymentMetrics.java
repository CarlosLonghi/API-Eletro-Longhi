package br.com.carloslonghi.eletrolonghi.metrics;

import br.com.carloslonghi.eletrolonghi.entity.enums.PaymentMethod;
import br.com.carloslonghi.eletrolonghi.entity.enums.PaymentStatus;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;

/**
 * Métricas dos pagamentos, expostas em /actuator/prometheus (porta de gerência):
 * eletrolonghi_payments_registered_total{method}, eletrolonghi_payments_status_changes_total{status},
 * eletrolonghi_payments_approved_amount_brl{method} (valor aprovado: _sum é o faturado, _count a
 * quantidade) e eletrolonghi_payments_checkout_links_total (links do Checkout Pro gerados).
 * Registradas zeradas na criação, pelo mesmo motivo de {@link RepairOrderMetrics}.
 * As chamadas ao Mercado Pago já saem em http_client_requests_seconds{client_name="api.mercadopago.com"}.
 */
@Component
public class PaymentMetrics {

    private final Map<PaymentMethod, Counter> registered = new EnumMap<>(PaymentMethod.class);
    private final Map<PaymentMethod, DistributionSummary> approvedAmount = new EnumMap<>(PaymentMethod.class);
    private final Map<PaymentStatus, Counter> statusChanges = new EnumMap<>(PaymentStatus.class);
    private final Counter checkoutLinks;

    public PaymentMetrics(MeterRegistry registry) {
        for (PaymentMethod method : PaymentMethod.values()) {
            registered.put(method, Counter.builder("eletrolonghi.payments.registered")
                    .description("Pagamentos registrados")
                    .tag("method", method.name())
                    .register(registry));
            approvedAmount.put(method, DistributionSummary.builder("eletrolonghi.payments.approved.amount")
                    .description("Valor dos pagamentos aprovados")
                    .baseUnit("brl")
                    .tag("method", method.name())
                    .register(registry));
        }
        for (PaymentStatus status : PaymentStatus.values()) {
            statusChanges.put(status, Counter.builder("eletrolonghi.payments.status_changes")
                    .description("Mudanças de status dos pagamentos, pelo status de destino")
                    .tag("status", status.name())
                    .register(registry));
        }
        checkoutLinks = Counter.builder("eletrolonghi.payments.checkout_links")
                .description("Links do Checkout Pro (Mercado Pago) gerados")
                .register(registry);
    }

    public void registered(PaymentMethod method) {
        registered.get(method).increment();
    }

    public void statusChanged(PaymentStatus status) {
        statusChanges.get(status).increment();
    }

    public void approved(PaymentMethod method, BigDecimal amount) {
        approvedAmount.get(method).record(amount.doubleValue());
    }

    public void checkoutLinkCreated() {
        checkoutLinks.increment();
    }
}
