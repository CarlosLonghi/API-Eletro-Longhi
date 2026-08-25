package br.com.carloslonghi.eletrolonghi.controller;

import br.com.carloslonghi.eletrolonghi.config.TokenService;
import br.com.carloslonghi.eletrolonghi.controller.request.LoginRequest;
import br.com.carloslonghi.eletrolonghi.controller.request.RefreshTokenRequest;
import br.com.carloslonghi.eletrolonghi.controller.request.UserRequest;
import br.com.carloslonghi.eletrolonghi.controller.response.LoginResponse;
import br.com.carloslonghi.eletrolonghi.controller.response.UserResponse;
import br.com.carloslonghi.eletrolonghi.entity.RefreshToken;
import br.com.carloslonghi.eletrolonghi.entity.User;
import br.com.carloslonghi.eletrolonghi.entity.enums.Role;
import br.com.carloslonghi.eletrolonghi.exception.UsernameOrPasswordInvalidException;
import br.com.carloslonghi.eletrolonghi.mapper.UserMapper;
import br.com.carloslonghi.eletrolonghi.service.LoginAttemptService;
import br.com.carloslonghi.eletrolonghi.service.RefreshTokenService;
import br.com.carloslonghi.eletrolonghi.service.UserService;
import br.com.carloslonghi.eletrolonghi.support.TestFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.Authentication;
import br.com.carloslonghi.eletrolonghi.exception.AccountNotActivatedException;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private TokenService tokenService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private LoginAttemptService loginAttemptService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserMapper userMapper;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthController authController;

    @Test
    void shouldRegisterUser() {
        UserRequest request = new UserRequest("User", "user@mail.com", "123");
        User user = TestFixtures.user(1L);
        UserResponse response = new UserResponse(1L, "User", "user@mail.com", Role.USER, false);

        when(userMapper.toEntity(request)).thenReturn(user);
        when(userService.save(user)).thenReturn(user);
        when(userMapper.toResponse(user)).thenReturn(response);

        var result = authController.registerUser(request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getBody()).isEqualTo(response);
    }

    @Test
    void shouldLoginSuccessfully() {
        User user = TestFixtures.user(1L);
        RefreshToken refreshToken = TestFixtures.refreshToken("refresh", user, Instant.now().plusSeconds(100), false);

        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(user);
        when(tokenService.generateToken(user)).thenReturn("jwt");
        when(refreshTokenService.createRefreshToken(user)).thenReturn(refreshToken);

        var result = authController.login(new LoginRequest(user.getEmail(), "senha"));

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(new LoginResponse("jwt", "refresh"));
        verify(loginAttemptService).checkBlocked(user.getEmail());
        verify(loginAttemptService).loginSucceeded(user.getEmail());
    }

    @Test
    void shouldFailLoginWithInvalidCredentials() {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad"));

        assertThatThrownBy(() -> authController.login(new LoginRequest("user@mail.com", "wrong")))
                .isInstanceOf(UsernameOrPasswordInvalidException.class)
                .hasMessageContaining("inválido");

        verify(loginAttemptService).loginFailed("user@mail.com");
    }

    @Test
    void shouldFailLoginWithDisabledAccount() {
        when(authenticationManager.authenticate(any())).thenThrow(new DisabledException("disabled"));

        assertThatThrownBy(() -> authController.login(new LoginRequest("user@mail.com", "senha")))
                .isInstanceOf(AccountNotActivatedException.class)
                .hasMessageContaining("ativação");

        verify(loginAttemptService).loginFailed("user@mail.com");
    }

    @Test
    void shouldRefreshToken() {
        User user = TestFixtures.user(1L);
        RefreshToken oldToken = TestFixtures.refreshToken("old", user, Instant.now().plusSeconds(100), false);
        RefreshToken newToken = TestFixtures.refreshToken("new", user, Instant.now().plusSeconds(200), false);

        when(refreshTokenService.findValidToken("old")).thenReturn(oldToken);
        when(tokenService.generateToken(user)).thenReturn("new-jwt");
        when(refreshTokenService.createRefreshToken(user)).thenReturn(newToken);

        var result = authController.refresh(new RefreshTokenRequest("old"));

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(new LoginResponse("new-jwt", "new"));
    }

    @Test
    void shouldLogout() {
        var result = authController.logout(new RefreshTokenRequest("refresh"));

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(refreshTokenService).revoke("refresh");
    }
}
