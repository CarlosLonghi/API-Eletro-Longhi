package br.com.carloslonghi.eletrolonghi.metrics;

import br.com.carloslonghi.eletrolonghi.entity.enums.RepairOrderStatus;
import br.com.carloslonghi.eletrolonghi.repository.RepairOrderRepository;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RepairOrderMetricsTest {

    private final SimpleMeterRegistry registry = new SimpleMeterRegistry();
    private final RepairOrderRepository repository = mock(RepairOrderRepository.class);
    private final RepairOrderMetrics metrics = new RepairOrderMetrics(registry, repository);

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
    void shouldReadOrdersPerStatusFromRepository() {
        when(repository.countByStatus(RepairOrderStatus.AWAITING_PARTS)).thenReturn(3L);

        assertThat(registry.get("eletrolonghi.repair_orders.by_status").tag("status", "AWAITING_PARTS").gauge().value())
                .isEqualTo(3);
    }
}
