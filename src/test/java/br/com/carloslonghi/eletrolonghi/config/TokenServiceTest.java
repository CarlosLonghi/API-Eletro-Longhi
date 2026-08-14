package br.com.carloslonghi.eletrolonghi.config;

import br.com.carloslonghi.eletrolonghi.entity.User;
import br.com.carloslonghi.eletrolonghi.support.TestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class TokenServiceTest {

    private TokenService tokenService;

    @BeforeEach
    void setUp() {
        tokenService = new TokenService();
        ReflectionTestUtils.setField(tokenService, "secret", "unit-test-secret");
        ReflectionTestUtils.setField(tokenService, "accessTokenExpirationSeconds", 300L);
    }

    @Test
    void shouldGenerateAndVerifyToken() {
        User user = TestFixtures.user(1L);

        String token = tokenService.generateToken(user);
        Optional<JWTUserData> verified = tokenService.verifyToken(token);

        assertThat(token).isNotBlank();
        assertThat(verified).isPresent();
        assertThat(verified.get().email()).isEqualTo(user.getEmail());
        assertThat(verified.get().role()).isEqualTo(user.getRole().name());
    }

    @Test
    void shouldReturnEmptyForInvalidToken() {
        Optional<JWTUserData> verified = tokenService.verifyToken("invalid-token");

        assertThat(verified).isEmpty();
    }
}
