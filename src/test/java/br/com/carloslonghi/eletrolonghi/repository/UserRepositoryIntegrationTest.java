package br.com.carloslonghi.eletrolonghi.repository;

import br.com.carloslonghi.eletrolonghi.entity.User;
import br.com.carloslonghi.eletrolonghi.entity.enums.Role;
import br.com.carloslonghi.eletrolonghi.repository.support.AbstractPostgresIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class UserRepositoryIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void clean() {
        userRepository.deleteAll();
    }

    @Test
    void shouldFindUserByEmail() {
        userRepository.save(User.builder()
                .name("Admin")
                .email("admin@mail.com")
                .password("encoded")
                .role(Role.ADMIN)
                .build());

        assertThat(userRepository.findUserByEmail("admin@mail.com")).isPresent();
        assertThat(userRepository.findUserByEmail("missing@mail.com")).isEmpty();
    }
}

