package br.com.carloslonghi.eletrolonghi.controller;

import br.com.carloslonghi.eletrolonghi.config.TokenService;
import br.com.carloslonghi.eletrolonghi.controller.api.spec.AuthApi;
import br.com.carloslonghi.eletrolonghi.controller.request.LoginRequest;
import br.com.carloslonghi.eletrolonghi.controller.request.RefreshTokenRequest;
import br.com.carloslonghi.eletrolonghi.controller.request.UserRequest;
import br.com.carloslonghi.eletrolonghi.controller.response.LoginResponse;
import br.com.carloslonghi.eletrolonghi.controller.response.UserResponse;
import br.com.carloslonghi.eletrolonghi.entity.RefreshToken;
import br.com.carloslonghi.eletrolonghi.entity.User;
import br.com.carloslonghi.eletrolonghi.exception.UsernameOrPasswordInvalidException;
import br.com.carloslonghi.eletrolonghi.mapper.UserMapper;
import br.com.carloslonghi.eletrolonghi.service.LoginAttemptService;
import br.com.carloslonghi.eletrolonghi.service.RefreshTokenService;
import br.com.carloslonghi.eletrolonghi.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController implements AuthApi {

    private final UserService userService;
    private final TokenService tokenService;
    private final RefreshTokenService refreshTokenService;
    private final LoginAttemptService loginAttemptService;

    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> registerUser(@Valid @RequestBody UserRequest request) {
        User userEntity = userMapper.toEntity(request);
        User userRegistered = userService.save(userEntity);

        log.info("Novo usuário registrado: {}", userRegistered.getEmail());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userMapper.toResponse(userRegistered));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        String email = request.email();
        loginAttemptService.checkBlocked(email);

        try {
            UsernamePasswordAuthenticationToken emailAndPassword = new UsernamePasswordAuthenticationToken(
                    email,
                    request.password()
            );

            Authentication authentication = authenticationManager.authenticate(emailAndPassword);

            User user = (User) authentication.getPrincipal();

            loginAttemptService.loginSucceeded(email);

            String token = tokenService.generateToken(user);
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

            log.info("Login bem-sucedido para o usuário: {}", email);

            return ResponseEntity.ok(new LoginResponse(token, refreshToken.getToken()));
        } catch (BadCredentialsException exception) {
            loginAttemptService.loginFailed(email);
            log.warn("Tentativa de login inválida para o e-mail: {}", email);

            throw new UsernameOrPasswordInvalidException("Usuário e(ou) senha inválido(s).");
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        RefreshToken currentRefreshToken = refreshTokenService.findValidToken(request.refreshToken());
        User user = currentRefreshToken.getUser();

        String newAccessToken = tokenService.generateToken(user);
        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user);

        log.info("Token de acesso renovado para o usuário: {}", user.getEmail());

        return ResponseEntity.ok(new LoginResponse(newAccessToken, newRefreshToken.getToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {
        refreshTokenService.revoke(request.refreshToken());

        log.info("Logout realizado; refresh token revogado.");

        return ResponseEntity.noContent().build();
    }
}
