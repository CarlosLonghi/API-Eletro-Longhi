package br.com.carloslonghi.eletrolonghi.controller;

import br.com.carloslonghi.eletrolonghi.controller.request.UserRoleUpdateRequest;
import br.com.carloslonghi.eletrolonghi.controller.request.UserStatusUpdateRequest;
import br.com.carloslonghi.eletrolonghi.controller.response.UserResponse;
import br.com.carloslonghi.eletrolonghi.entity.User;
import br.com.carloslonghi.eletrolonghi.entity.enums.Role;
import br.com.carloslonghi.eletrolonghi.mapper.UserMapper;
import br.com.carloslonghi.eletrolonghi.service.UserService;
import br.com.carloslonghi.eletrolonghi.support.TestFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserController userController;

    @Test
    void shouldReturnPagedUsers() {
        User user = TestFixtures.user(1L);
        UserResponse response = new UserResponse(1L, user.getName(), user.getEmail(), Role.USER, false);
        when(userService.findAll(any(), any(), any(), any(), any())).thenReturn(new PageImpl<>(List.of(user)));
        when(userMapper.toResponse(user)).thenReturn(response);

        var result = userController.getAllUsers(null, null, null, null, 0, 10, "id", "asc");

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().getContent()).containsExactly(response);
    }

    @Test
    void shouldUpdateUserRoleWhenFound() {
        User user = TestFixtures.user(1L);
        UserResponse response = new UserResponse(1L, user.getName(), user.getEmail(), Role.ADMIN, true);
        when(userService.updateRole(1L, Role.ADMIN)).thenReturn(Optional.of(user));
        when(userMapper.toResponse(user)).thenReturn(response);

        var result = userController.updateUserRole(1L, new UserRoleUpdateRequest(Role.ADMIN));

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(response);
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingRoleForMissingUser() {
        when(userService.updateRole(1L, Role.ADMIN)).thenReturn(Optional.empty());

        var result = userController.updateUserRole(1L, new UserRoleUpdateRequest(Role.ADMIN));

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldUpdateUserStatusWhenFound() {
        User user = TestFixtures.user(1L);
        UserResponse response = new UserResponse(1L, user.getName(), user.getEmail(), Role.USER, false);
        when(userService.updateStatus(1L, false)).thenReturn(Optional.of(user));
        when(userMapper.toResponse(user)).thenReturn(response);

        var result = userController.updateUserStatus(1L, new UserStatusUpdateRequest(false));

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(response);
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingStatusForMissingUser() {
        when(userService.updateStatus(1L, false)).thenReturn(Optional.empty());

        var result = userController.updateUserStatus(1L, new UserStatusUpdateRequest(false));

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
