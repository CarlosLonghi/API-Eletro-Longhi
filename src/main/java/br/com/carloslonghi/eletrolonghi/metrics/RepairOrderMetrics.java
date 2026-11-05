package br.com.carloslonghi.eletrolonghi.metrics;

import br.com.carloslonghi.eletrolonghi.entity.enums.RepairOrderStatus;
import br.com.carloslonghi.eletrolonghi.repository.RepairOrderRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.LongSupplier;

/**
 * Métricas das ordens de reparo, expostas em /actuator/prometheus (porta de gerência):
 * eletrolonghi_repair_orders_opened_total, eletrolonghi_repair_orders_status_changes_total{status}
 * (status de destino) e eletrolonghi_repair_orders_by_status{status} (quantas ordens estão em cada status agora;
 * as excluídas ficam de fora pelo {@code @SoftDelete}).
 * Contadores registrados zerados na criação: a série existe desde o startup, o Grafana mostra 0
 * em vez de "No data" e o increase() do Prometheus conta o primeiro evento.
 * <p>
 * O gauge por status lê de um snapshot: uma coleta lê os 8 gauges em sequência, e o primeiro dispara uma
 * única query agrupada ({@link RepairOrderRepository#countGroupedByStatus()}) que os demais reaproveitam
 * enquanto o snapshot tiver menos de {@link #SNAPSHOT_TTL} (bem abaixo do scrape_interval de 15s).
 * Assim cada coleta custa 1 query em vez de uma por status.
 */
@Component
public class RepairOrderMetrics {

    static final Duration SNAPSHOT_TTL = Duration.ofSeconds(5);

    private final Counter opened;
    private final Map<RepairOrderStatus, Counter> statusChanges = new EnumMap<>(RepairOrderStatus.class);
    private final RepairOrderRepository repairOrderRepository;
    private final LongSupplier nanoTime;

    private Map<RepairOrderStatus, Long> snapshot;
    private long snapshotTakenAt;

    @Autowired
    public RepairOrderMetrics(MeterRegistry registry, RepairOrderRepository repairOrderRepository) {
        this(registry, repairOrderRepository, System::nanoTime);
    }

    RepairOrderMetrics(MeterRegistry registry, RepairOrderRepository repairOrderRepository, LongSupplier nanoTime) {
        this.repairOrderRepository = repairOrderRepository;
        this.nanoTime = nanoTime;
        opened = Counter.builder("eletrolonghi.repair_orders.opened")
                .description("Ordens de reparo abertas")
                .register(registry);
        for (RepairOrderStatus status : RepairOrderStatus.values()) {
            statusChanges.put(status, Counter.builder("eletrolonghi.repair_orders.status_changes")
                    .description("Mudanças de status das ordens de reparo, pelo status de destino")
                    .tag("status", status.name())
                    .register(registry));
            Gauge.builder("eletrolonghi.repair_orders.by_status", this, metrics -> metrics.countFor(status))
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

    private synchronized long countFor(RepairOrderStatus status) {
        long now = nanoTime.getAsLong();
        if (snapshot == null || now - snapshotTakenAt >= SNAPSHOT_TTL.toNanos()) {
            Map<RepairOrderStatus, Long> fresh = new EnumMap<>(RepairOrderStatus.class);
            for (RepairOrderRepository.StatusCount row : repairOrderRepository.countGroupedByStatus()) {
                fresh.put(row.getStatus(), row.getTotal());
            }
            snapshot = fresh;
            snapshotTakenAt = now;
        }
        return snapshot.getOrDefault(status, 0L);
    }
}
