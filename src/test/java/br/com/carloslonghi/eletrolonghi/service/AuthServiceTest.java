package br.com.carloslonghi.eletrolonghi.service;

import br.com.carloslonghi.eletrolonghi.entity.User;
import br.com.carloslonghi.eletrolonghi.repository.UserRepository;
import br.com.carloslonghi.eletrolonghi.support.TestFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthService authService;

    @Test
    void shouldLoadUserByEmail() {
        User user = TestFixtures.user(1L);
        when(userRepository.findUserByEmail(user.getEmail())).thenReturn(Optional.of(user));

        assertThat(authService.loadUserByUsername(user.getEmail())).isEqualTo(user);
    }

    @Test
    void shouldThrowWhenUserNotFound() {
        when(userRepository.findUserByEmail("missing@mail.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.loadUserByUsername("missing@mail.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("Email ou Senha inválido");
    }
}
