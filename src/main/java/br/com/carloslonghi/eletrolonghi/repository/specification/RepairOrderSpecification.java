package br.com.carloslonghi.eletrolonghi.repository.specification;

import br.com.carloslonghi.eletrolonghi.entity.RepairOrder;
import br.com.carloslonghi.eletrolonghi.entity.enums.PaymentStatus;
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
            PaymentStatus paymentStatus,
            Long customerId,
            Long deviceId,
            LocalDateTime createdFrom,
            LocalDateTime createdTo
    ) {
        return (root, query, builder) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            if (status != null) {
                predicates.add(builder.equal(root.get("status"), status));
            }

            if (paymentStatus != null) {
                predicates.add(builder.equal(root.get("payment").get("status"), paymentStatus));
            }

            if (customerId != null) {
                predicates.add(builder.equal(root.get("customer").get("id"), customerId));
            }

            if (deviceId != null) {
                predicates.add(builder.equal(root.get("device").get("id"), deviceId));
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

