package br.com.carloslonghi.eletrolonghi.service;

import br.com.carloslonghi.eletrolonghi.entity.RefreshToken;
import br.com.carloslonghi.eletrolonghi.entity.User;
import br.com.carloslonghi.eletrolonghi.exception.InvalidRefreshTokenException;
import br.com.carloslonghi.eletrolonghi.repository.RefreshTokenRepository;
import br.com.carloslonghi.eletrolonghi.support.TestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(refreshTokenService, "refreshTokenExpirationMs", 10_000L);
    }

    @Test
    void shouldCreateRefreshTokenAndRevokePrevious() {
        User user = TestFixtures.user(1L);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RefreshToken created = refreshTokenService.createRefreshToken(user);

        assertThat(created.getUser()).isEqualTo(user);
        assertThat(created.isRevoked()).isFalse();
        assertThat(created.getToken()).isNotBlank();
        verify(refreshTokenRepository).revokeAllByUser(user);
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void shouldReturnValidToken() {
        RefreshToken token = TestFixtures.refreshToken("rt", TestFixtures.user(1L), Instant.now().plusSeconds(10), false);
        when(refreshTokenRepository.findByToken("rt")).thenReturn(Optional.of(token));

        RefreshToken result = refreshTokenService.findValidToken("rt");

        assertThat(result).isEqualTo(token);
    }

    @Test
    void shouldFailForRevokedToken() {
        RefreshToken token = TestFixtures.refreshToken("rt", TestFixtures.user(1L), Instant.now().plusSeconds(10), true);
        when(refreshTokenRepository.findByToken("rt")).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> refreshTokenService.findValidToken("rt"))
                .isInstanceOf(InvalidRefreshTokenException.class)
                .hasMessageContaining("revogado");
    }

    @Test
    void shouldFailForExpiredToken() {
        RefreshToken token = TestFixtures.refreshToken("rt", TestFixtures.user(1L), Instant.now().minusSeconds(10), false);
        when(refreshTokenRepository.findByToken("rt")).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> refreshTokenService.findValidToken("rt"))
                .isInstanceOf(InvalidRefreshTokenException.class)
                .hasMessageContaining("expirado");
    }

    @Test
    void shouldNotFailWhenRevokingUnknownToken() {
        when(refreshTokenRepository.findByToken("missing")).thenReturn(Optional.empty());

        refreshTokenService.revoke("missing");

        verify(refreshTokenRepository).findByToken("missing");
    }

    @Test
    void shouldFailWhenTokenDoesNotExist() {
        when(refreshTokenRepository.findByToken("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> refreshTokenService.findValidToken("missing"))
                .isInstanceOf(InvalidRefreshTokenException.class)
                .hasMessageContaining("inválido");
    }

    @Test
    void shouldRevokeExistingToken() {
        RefreshToken token = TestFixtures.refreshToken("rt", TestFixtures.user(1L), Instant.now().plusSeconds(30), false);
        when(refreshTokenRepository.findByToken("rt")).thenReturn(Optional.of(token));

        refreshTokenService.revoke("rt");

        assertThat(token.isRevoked()).isTrue();
        verify(refreshTokenRepository).save(token);
    }
}


