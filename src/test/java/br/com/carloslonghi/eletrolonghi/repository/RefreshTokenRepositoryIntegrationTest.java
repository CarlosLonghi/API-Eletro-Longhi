package br.com.carloslonghi.eletrolonghi.repository;

import br.com.carloslonghi.eletrolonghi.entity.RefreshToken;
import br.com.carloslonghi.eletrolonghi.entity.User;
import br.com.carloslonghi.eletrolonghi.entity.enums.Role;
import br.com.carloslonghi.eletrolonghi.repository.support.AbstractPostgresIntegrationTest;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class RefreshTokenRepositoryIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void clean() {
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void shouldFindAndRevokeAllTokensByUser() {
        User user = userRepository.save(User.builder()
                .name("Token User")
                .email("token-user@mail.com")
                .password("encoded")
                .role(Role.USER)
                .build());

        refreshTokenRepository.save(RefreshToken.builder()
                .token("token-a")
                .user(user)
                .expiryDate(Instant.now().plusSeconds(600))
                .revoked(false)
                .build());

        refreshTokenRepository.revokeAllByUser(user);
        entityManager.clear();

        RefreshToken refreshed = refreshTokenRepository.findByToken("token-a").orElseThrow();
        assertThat(refreshed.isRevoked()).isTrue();
    }
}

