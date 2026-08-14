package br.com.carloslonghi.eletrolonghi.controller.support;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;

import static org.assertj.core.api.Assertions.assertThat;

class PaginationUtilsTest {

    @Test
    void shouldCreateAscendingPageableByDefault() {
        Pageable pageable = PaginationUtils.createPageable(0, 10, "id", "invalid");

        assertThat(pageable.getPageNumber()).isEqualTo(0);
        assertThat(pageable.getPageSize()).isEqualTo(10);
        assertThat(pageable.getSort().getOrderFor("id").isAscending()).isTrue();
    }

    @Test
    void shouldCreateDescendingPageable() {
        Pageable pageable = PaginationUtils.createPageable(1, 20, "name", "desc");

        assertThat(pageable.getPageNumber()).isEqualTo(1);
        assertThat(pageable.getPageSize()).isEqualTo(20);
        assertThat(pageable.getSort().getOrderFor("name").isDescending()).isTrue();
    }
}
