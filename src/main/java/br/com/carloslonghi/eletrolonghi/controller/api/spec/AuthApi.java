package br.com.carloslonghi.eletrolonghi.controller.api.spec;

import br.com.carloslonghi.eletrolonghi.controller.request.LoginRequest;
import br.com.carloslonghi.eletrolonghi.controller.request.UserRequest;
import br.com.carloslonghi.eletrolonghi.controller.response.LoginResponse;
import br.com.carloslonghi.eletrolonghi.controller.response.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(
        name = "Autenticação",
        description = "Operações para registro e login de usuários"
)
public interface AuthApi {
    @Operation(
            summary = "Registrar novo usuário",
            description = "Registra um novo usuário para acessar a aplicação"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Usuário registrado com sucesso",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Dados da request inválidos", content = @Content),
            @ApiResponse(responseCode = "409", description = "Usuário já cadastrado", content = @Content)
    })
    ResponseEntity<UserResponse> registerUser(
            @RequestBody(
                    description = "Dados para cadastro do usuário",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = UserRequest.class)
                    )
            )
            UserRequest request
    );

    @Operation(
            summary = "Realizar login do usuário",
            description = "Verifica email e senha; retorna JWT para autenticação se estiverem corretos"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Token retornado com sucesso",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoginResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Dados da request inválidos", content = @Content),
            @ApiResponse(responseCode = "401", description = "Email e(ou) senha incorreto(s)", content = @Content)
    })
    ResponseEntity<LoginResponse> login(
            @RequestBody(
                    description = "Dados para login do usuário",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = LoginRequest.class)
                    )
            )
            LoginRequest request
    );

}
