package br.com.carloslonghi.eletrolonghi.repository;

import br.com.carloslonghi.eletrolonghi.entity.RepairOrder;
import br.com.carloslonghi.eletrolonghi.entity.enums.RepairOrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RepairOrderRepository extends JpaRepository<RepairOrder, Long>, JpaSpecificationExecutor<RepairOrder> {

    boolean existsByDeviceIdAndStatusNot(Long deviceId, RepairOrderStatus status);

    boolean existsByCustomerId(Long customerId);

    boolean existsByDeviceId(Long deviceId);

    /**
     * Quantas ordens existem em cada status, numa única query (alimenta o gauge
     * {@code eletrolonghi.repair_orders.by_status}). Status sem nenhuma ordem não aparecem
     * no resultado; o {@code @SoftDelete} deixa as excluídas de fora.
     */
    @Query("select ro.status as status, count(ro) as total from RepairOrder ro group by ro.status")
    List<StatusCount> countGroupedByStatus();

    interface StatusCount {
        RepairOrderStatus getStatus();

        long getTotal();
    }

    /**
     * Listagem paginada: puxa {@code customer}, {@code device} e {@code payment} (todas
     * associações {@code *ToOne}) numa única query, evitando N+1 na serialização e o
     * SELECT extra por linha do {@code @OneToOne(mappedBy)} de {@code payment}.
     */
    @Override
    @EntityGraph(attributePaths = {"customer", "device", "payment"})
    Page<RepairOrder> findAll(Specification<RepairOrder> spec, Pageable pageable);
}
