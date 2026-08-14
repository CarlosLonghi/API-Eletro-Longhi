package br.com.carloslonghi.eletrolonghi.service;

import br.com.carloslonghi.eletrolonghi.entity.User;
import br.com.carloslonghi.eletrolonghi.repository.UserRepository;
import br.com.carloslonghi.eletrolonghi.support.TestFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void shouldEncodePasswordBeforeSaving() {
        User user = TestFixtures.user(1L);
        when(passwordEncoder.encode("senha")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.save(user);

        assertThat(result.getPassword()).isEqualTo("encoded");
        verify(passwordEncoder).encode("senha");
        verify(userRepository).save(user);
    }
}

