package br.com.carloslonghi.eletrolonghi.entity;

import br.com.carloslonghi.eletrolonghi.entity.enums.RepairOrderStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SoftDelete;
import org.hibernate.annotations.SoftDeleteType;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "repair_orders")
@SoftDelete(strategy = SoftDeleteType.TIMESTAMP, columnName = "deleted_at")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepairOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RepairOrderStatus status;

    @Column(name = "estimated_cost", precision = 12, scale = 2)
    private BigDecimal estimatedCost;

    @Column(name = "estimated_completion_date")
    private LocalDate estimatedCompletionDate;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(optional = false)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    /**
     * Pagamento da ordem — 1:1, lado inverso (a FK {@code repair_order_id} vive em
     * {@code payments}). {@code null} enquanto a ordem não tem pagamento registrado.
     *
     * <p>{@code Payment} <b>não</b> usa {@code @SoftDelete}: como esta associação é um
     * {@code @OneToOne} inverso e é carregada junto com a ordem, o predicado de soft
     * delete no join faz o Hibernate lançar {@code FetchNotFoundException} para toda
     * ordem sem pagamento. {@code DELETE /payment/{id}} é, portanto, remoção física.
     */
    @OneToOne(mappedBy = "repairOrder")
    private Payment payment;
}
