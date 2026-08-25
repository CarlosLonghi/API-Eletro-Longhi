package br.com.carloslonghi.eletrolonghi.service;

import br.com.carloslonghi.eletrolonghi.entity.User;
import br.com.carloslonghi.eletrolonghi.entity.enums.Role;
import br.com.carloslonghi.eletrolonghi.repository.UserRepository;
import br.com.carloslonghi.eletrolonghi.support.TestFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

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

    @Test
    void shouldDisableNewlyRegisteredUserOnSave() {
        User user = TestFixtures.user(1L);
        user.setEnabled(true);
        when(passwordEncoder.encode("senha")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.save(user);

        assertThat(result.isEnabled()).isFalse();
    }

    @Test
    void shouldUpdateOnlyRoleWithoutTouchingOtherFields() {
        User user = TestFixtures.user(1L);
        user.setEnabled(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        Optional<User> updated = userService.updateRole(1L, Role.ADMIN);

        assertThat(updated).isPresent();
        assertThat(user.getRole()).isEqualTo(Role.ADMIN);
        assertThat(user.isEnabled()).isTrue();
        verify(userRepository).save(user);
    }

    @Test
    void shouldUpdateOnlyStatusWithoutTouchingRole() {
        User user = TestFixtures.user(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        Optional<User> updated = userService.updateStatus(1L, false);

        assertThat(updated).isPresent();
        assertThat(user.isEnabled()).isFalse();
        assertThat(user.getRole()).isEqualTo(Role.USER);
        verify(userRepository).save(user);
    }

    @Test
    void shouldReturnEmptyWhenUpdatingRoleOrStatusForMissingUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThat(userService.updateRole(1L, Role.ADMIN)).isEmpty();
        assertThat(userService.updateStatus(1L, true)).isEmpty();
    }

    @Test
    void shouldFindAllUsersWithFilters() {
        Page<User> page = new PageImpl<>(List.of(TestFixtures.user(1L)));
        when(userRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(page);

        Page<User> result = userService.findAll("Usuario", "user1@mail.com", Role.USER, true, PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(1);
    }
}

