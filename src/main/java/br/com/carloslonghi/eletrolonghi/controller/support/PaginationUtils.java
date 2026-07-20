package br.com.carloslonghi.eletrolonghi.controller.support;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public final class PaginationUtils {

    private PaginationUtils() {
    }

    public static Pageable createPageable(int page, int size, String sortBy, String direction) {
        Sort.Direction sortDirection = Sort.Direction.fromOptionalString(direction)
                .orElse(Sort.Direction.ASC);

        return PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
    }
}

