package br.com.carloslonghi.eletrolonghi.metrics;

import br.com.carloslonghi.eletrolonghi.entity.enums.RepairOrderStatus;
import br.com.carloslonghi.eletrolonghi.repository.RepairOrderRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

/**
 * Métricas das ordens de reparo, expostas em /actuator/prometheus (porta de gerência):
 * eletrolonghi_repair_orders_opened_total, eletrolonghi_repair_orders_status_changes_total{status}
 * (status de destino) e eletrolonghi_repair_orders_by_status{status} (quantas ordens estão em cada status agora,
 * contadas no banco a cada coleta; as excluídas ficam de fora pelo {@code @SoftDelete}).
 * Contadores registrados zerados na criação: a série existe desde o startup, o Grafana mostra 0
 * em vez de "No data" e o increase() do Prometheus conta o primeiro evento.
 */
@Component
public class RepairOrderMetrics {

    private final Counter opened;
    private final Map<RepairOrderStatus, Counter> statusChanges = new EnumMap<>(RepairOrderStatus.class);

    public RepairOrderMetrics(MeterRegistry registry, RepairOrderRepository repairOrderRepository) {
        opened = Counter.builder("eletrolonghi.repair_orders.opened")
                .description("Ordens de reparo abertas")
                .register(registry);
        for (RepairOrderStatus status : RepairOrderStatus.values()) {
            statusChanges.put(status, Counter.builder("eletrolonghi.repair_orders.status_changes")
                    .description("Mudanças de status das ordens de reparo, pelo status de destino")
                    .tag("status", status.name())
                    .register(registry));
            Gauge.builder("eletrolonghi.repair_orders.by_status", repairOrderRepository, repo -> repo.countByStatus(status))
                    .description("Ordens de reparo em cada status")
                    .tag("status", status.name())
                    .register(registry);
        }
    }

    public void opened() {
        opened.increment();
    }

    public void statusChanged(RepairOrderStatus status) {
        statusChanges.get(status).increment();
    }
}
