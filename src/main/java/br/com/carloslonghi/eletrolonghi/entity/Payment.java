package br.com.carloslonghi.eletrolonghi.entity;

import br.com.carloslonghi.eletrolonghi.entity.enums.PaymentMethod;
import br.com.carloslonghi.eletrolonghi.entity.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * {@code Payment} é a única entidade apagável <b>sem</b> {@code @SoftDelete}: ela é o
 * lado inverso de um {@code @OneToOne} carregado junto com {@code RepairOrder}, e o
 * predicado de soft delete no join quebra o carregamento de toda ordem sem pagamento
 * ({@code FetchNotFoundException}). Além disso {@code repair_order_id} é {@code UNIQUE},
 * então uma linha "removida logicamente" bloquearia um novo pagamento para a ordem.
 * {@code DELETE /payment/{id}} é remoção física.
 */
@Entity
@Table(name = "payments")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod method;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Column(nullable = false)
    private Integer installments;

    private String description;

    @Column(name = "payer_name")
    private String payerName;

    @Column(name = "payer_document")
    private String payerDocument;

    @Column(name = "external_reference")
    private String externalReference;

    @Column(name = "gateway_payment_id")
    private String gatewayPaymentId;

    @OneToOne(optional = false)
    @JoinColumn(name = "repair_order_id", nullable = false, unique = true)
    private RepairOrder repairOrder;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
