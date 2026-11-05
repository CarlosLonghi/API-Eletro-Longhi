package br.com.carloslonghi.eletrolonghi.metrics;

import br.com.carloslonghi.eletrolonghi.entity.enums.RepairOrderStatus;
import br.com.carloslonghi.eletrolonghi.repository.RepairOrderRepository;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RepairOrderMetricsTest {

    private final SimpleMeterRegistry registry = new SimpleMeterRegistry();
    private final RepairOrderRepository repository = mock(RepairOrderRepository.class);
    private final AtomicLong clock = new AtomicLong();
    private final RepairOrderMetrics metrics = new RepairOrderMetrics(registry, repository, clock::get);

    @Test
    void shouldRegisterEveryStatusZeroed() {
        assertThat(registry.get("eletrolonghi.repair_orders.opened").counter().count()).isZero();
        for (RepairOrderStatus status : RepairOrderStatus.values()) {
            assertThat(registry.get("eletrolonghi.repair_orders.status_changes").tag("status", status.name())
                    .counter().count()).isZero();
        }
    }

    @Test
    void shouldCountCreatedAndStatusChanges() {
        metrics.opened();
        metrics.statusChanged(RepairOrderStatus.IN_REPAIR);
        metrics.statusChanged(RepairOrderStatus.IN_REPAIR);

        assertThat(registry.get("eletrolonghi.repair_orders.opened").counter().count()).isEqualTo(1);
        assertThat(registry.get("eletrolonghi.repair_orders.status_changes").tag("status", "IN_REPAIR")
                .counter().count()).isEqualTo(2);
        assertThat(registry.get("eletrolonghi.repair_orders.status_changes").tag("status", "APPROVED")
                .counter().count()).isZero();
    }

    @Test
    void shouldReadOrdersPerStatusFromOneGroupedQuery() {
        when(repository.countGroupedByStatus()).thenReturn(List.of(
                statusCount(RepairOrderStatus.AWAITING_PARTS, 3),
                statusCount(RepairOrderStatus.IN_REPAIR, 2)));

        assertThat(gauge("AWAITING_PARTS")).isEqualTo(3);
        assertThat(gauge("IN_REPAIR")).isEqualTo(2);
        assertThat(gauge("APPROVED")).isZero();

        verify(repository, times(1)).countGroupedByStatus();
    }

    @Test
    void shouldRefreshSnapshotAfterTtl() {
        when(repository.countGroupedByStatus())
                .thenReturn(List.of(statusCount(RepairOrderStatus.IN_REPAIR, 1)))
                .thenReturn(List.of(statusCount(RepairOrderStatus.IN_REPAIR, 4)));

        assertThat(gauge("IN_REPAIR")).isEqualTo(1);
        clock.addAndGet(RepairOrderMetrics.SNAPSHOT_TTL.toNanos() - 1);
        assertThat(gauge("IN_REPAIR")).isEqualTo(1);
        clock.addAndGet(1);
        assertThat(gauge("IN_REPAIR")).isEqualTo(4);

        verify(repository, times(2)).countGroupedByStatus();
    }

    private double gauge(String status) {
        return registry.get("eletrolonghi.repair_orders.by_status").tag("status", status).gauge().value();
    }

    private static RepairOrderRepository.StatusCount statusCount(RepairOrderStatus status, long total) {
        return new RepairOrderRepository.StatusCount() {
            @Override
            public RepairOrderStatus getStatus() {
                return status;
            }

            @Override
            public long getTotal() {
                return total;
            }
        };
    }
}
