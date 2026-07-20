package br.com.carloslonghi.eletrolonghi.repository.specification;

import br.com.carloslonghi.eletrolonghi.entity.Device;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class DeviceSpecification {

    private DeviceSpecification() {
    }

    public static Specification<Device> withFilters(
            String model,
            String serialNumber,
            Long brandId,
            Long accessoryId
    ) {
        return (root, query, builder) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            if (query != null) {
                query.distinct(true);
            }

            if (model != null && !model.isBlank()) {
                predicates.add(builder.like(
                        builder.lower(root.get("model")),
                        "%" + model.trim().toLowerCase() + "%"
                ));
            }

            if (serialNumber != null && !serialNumber.isBlank()) {
                predicates.add(builder.like(
                        builder.lower(root.get("serialNumber")),
                        "%" + serialNumber.trim().toLowerCase() + "%"
                ));
            }

            if (brandId != null) {
                predicates.add(builder.equal(root.get("brand").get("id"), brandId));
            }

            if (accessoryId != null) {
                predicates.add(builder.equal(root.join("accessories").get("id"), accessoryId));
            }

            return predicates.isEmpty()
                    ? builder.conjunction()
                    : builder.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }
}

