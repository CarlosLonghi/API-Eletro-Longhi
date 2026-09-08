package br.com.carloslonghi.eletrolonghi.repository;

import br.com.carloslonghi.eletrolonghi.entity.RepairOrder;
import br.com.carloslonghi.eletrolonghi.entity.enums.RepairOrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface RepairOrderRepository extends JpaRepository<RepairOrder, Long>, JpaSpecificationExecutor<RepairOrder> {

    boolean existsByDeviceIdAndStatusNot(Long deviceId, RepairOrderStatus status);

    /**
     * Listagem paginada: puxa {@code customer}, {@code device} e {@code payment} (todas
     * associações {@code *ToOne}) numa única query, evitando N+1 na serialização e o
     * SELECT extra por linha do {@code @OneToOne(mappedBy)} de {@code payment}.
     */
    @Override
    @EntityGraph(attributePaths = {"customer", "device", "payment"})
    Page<RepairOrder> findAll(Specification<RepairOrder> spec, Pageable pageable);
}
