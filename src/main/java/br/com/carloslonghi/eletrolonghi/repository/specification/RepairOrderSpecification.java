package br.com.carloslonghi.eletrolonghi.repository.specification;

import br.com.carloslonghi.eletrolonghi.entity.RepairOrder;
import br.com.carloslonghi.eletrolonghi.entity.enums.RepairOrderStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class RepairOrderSpecification {

    private RepairOrderSpecification() {
    }

    public static Specification<RepairOrder> withFilters(
            RepairOrderStatus status,
            Long customerId,
            Long deviceId,
            String description,
            LocalDateTime createdFrom,
            LocalDateTime createdTo
    ) {
        return (root, query, builder) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            if (status != null) {
                predicates.add(builder.equal(root.get("status"), status));
            }

            if (customerId != null) {
                predicates.add(builder.equal(root.get("customer").get("id"), customerId));
            }

            if (deviceId != null) {
                predicates.add(builder.equal(root.get("device").get("id"), deviceId));
            }

            if (description != null && !description.isBlank()) {
                predicates.add(builder.like(
                        builder.lower(root.get("description")),
                        "%" + description.trim().toLowerCase() + "%"
                ));
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

