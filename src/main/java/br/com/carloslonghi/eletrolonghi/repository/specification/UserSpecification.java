package br.com.carloslonghi.eletrolonghi.repository.specification;

import br.com.carloslonghi.eletrolonghi.entity.User;
import br.com.carloslonghi.eletrolonghi.entity.enums.Role;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class UserSpecification {

    private UserSpecification() {
    }

    public static Specification<User> withFilters(String name, String email, Role role, Boolean enabled) {
        return (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();

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

            if (role != null) {
                predicates.add(builder.equal(root.get("role"), role));
            }

            if (enabled != null) {
                predicates.add(builder.equal(root.get("enabled"), enabled));
            }

            return predicates.isEmpty()
                    ? builder.conjunction()
                    : builder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
