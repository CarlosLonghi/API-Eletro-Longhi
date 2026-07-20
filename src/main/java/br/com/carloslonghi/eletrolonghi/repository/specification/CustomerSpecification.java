package br.com.carloslonghi.eletrolonghi.repository.specification;

import br.com.carloslonghi.eletrolonghi.entity.Customer;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class CustomerSpecification {

    private CustomerSpecification() {
    }

    public static Specification<Customer> withFilters(String name, String email, String phone) {
        return (root, query, builder) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            if (name != null && !name.isBlank()) {
                predicates.add(builder.like(
                        builder.lower(root.get("name")),
                        "%" + name.trim().toLowerCase() + "%"
                ));
            }

            if (email != null && !email.isBlank()) {
                predicates.add(builder.like(
                        builder.lower(root.get("email")),
                        "%" + email.trim().toLowerCase() + "%"
                ));
            }

            if (phone != null && !phone.isBlank()) {
                predicates.add(builder.like(
                        builder.lower(root.get("phone")),
                        "%" + phone.trim().toLowerCase() + "%"
                ));
            }

            return predicates.isEmpty()
                    ? builder.conjunction()
                    : builder.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }
}

