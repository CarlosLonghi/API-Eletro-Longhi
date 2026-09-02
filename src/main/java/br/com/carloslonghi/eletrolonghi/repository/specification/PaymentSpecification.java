package br.com.carloslonghi.eletrolonghi.repository.specification;

import br.com.carloslonghi.eletrolonghi.entity.Payment;
import br.com.carloslonghi.eletrolonghi.entity.enums.PaymentMethod;
import br.com.carloslonghi.eletrolonghi.entity.enums.PaymentStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class PaymentSpecification {

    private PaymentSpecification() {
    }

    public static Specification<Payment> withFilters(
            PaymentStatus status,
            PaymentMethod method,
            Long repairOrderId,
            LocalDateTime createdFrom,
            LocalDateTime createdTo
    ) {
        return (root, query, builder) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            if (status != null) {
                predicates.add(builder.equal(root.get("status"), status));
            }

            if (method != null) {
                predicates.add(builder.equal(root.get("method"), method));
            }

            if (repairOrderId != null) {
                predicates.add(builder.equal(root.get("repairOrder").get("id"), repairOrderId));
            }

            if (createdFrom != null) {
                predicates.add(builder.greaterThanOrEqualTo(root.get("createdAt"), createdFrom));
            }

            if (createdTo != null) {
                predicates.add(builder.lessThanOrEqualTo(root.get("createdAt"), createdTo));
            }

            return predicates.isEmpty()
                    ? builder.conjunction()
                    : builder.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }
}
