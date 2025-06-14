package br.com.carloslonghi.eletrolonghi.controller;

import br.com.carloslonghi.eletrolonghi.config.TokenService;
import br.com.carloslonghi.eletrolonghi.controller.request.LoginRequest;
import br.com.carloslonghi.eletrolonghi.controller.request.UserRequest;
import br.com.carloslonghi.eletrolonghi.controller.response.LoginResponse;
import br.com.carloslonghi.eletrolonghi.controller.response.UserResponse;
import br.com.carloslonghi.eletrolonghi.entity.User;
import br.com.carloslonghi.eletrolonghi.exception.UsernameOrPasswordInvalidException;
import br.com.carloslonghi.eletrolonghi.mapper.UserMapper;
import br.com.carloslonghi.eletrolonghi.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final TokenService tokenService;

    private final AuthenticationManager authenticationManager;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> registerUser(@Valid @RequestBody UserRequest request) {
        User userEntity = UserMapper.toUserEntity(request);
        User userRegistered = userService.save(userEntity);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(UserMapper.toUserResponse(userRegistered));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        try {
            UsernamePasswordAuthenticationToken emailAndPassword = new UsernamePasswordAuthenticationToken(
                    request.email(),
                    request.password()
            );

            Authentication authentication = authenticationManager.authenticate(emailAndPassword);

            User user = (User) authentication.getPrincipal();

            String token = tokenService.generateToken(user);

            return ResponseEntity.ok(new LoginResponse(token));
        } catch (BadCredentialsException exception) {
            throw new UsernameOrPasswordInvalidException("Usuário e(ou) senha inválido(s).");
        }
    }
}
