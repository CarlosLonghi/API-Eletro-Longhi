package br.com.carloslonghi.eletrolonghi.controller.api.spec;

import br.com.carloslonghi.eletrolonghi.controller.request.UserRoleUpdateRequest;
import br.com.carloslonghi.eletrolonghi.controller.request.UserStatusUpdateRequest;
import br.com.carloslonghi.eletrolonghi.controller.response.UserResponse;
import br.com.carloslonghi.eletrolonghi.entity.enums.Role;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(
        name = "Usuário",
        description = "Operações administrativas de gerenciamento de usuários. Requer perfil ADMIN."
)
@SecurityRequirement(name = "bearerAuth")
public interface UserApi {

    @Operation(
            summary = "Listar todos os usuários",
            description = "Retorna a lista paginada de usuários cadastrados. Requer perfil ADMIN."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista retornada com sucesso",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = UserResponse.class))
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Token de autenticação ausente, inválido ou expirado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Não autorizado — requer perfil ADMIN", content = @Content)
    })
    ResponseEntity<Page<UserResponse>> getAllUsers(
            @Parameter(in = ParameterIn.QUERY, description = "Filtro parcial por nome")
            @RequestParam(required = false) String name,
            @Parameter(in = ParameterIn.QUERY, description = "Filtro parcial por e-mail")
            @RequestParam(required = false) String email,
            @Parameter(in = ParameterIn.QUERY, description = "Filtro por papel", schema = @Schema(implementation = Role.class))
            @RequestParam(required = false) Role role,
            @Parameter(in = ParameterIn.QUERY, description = "Filtro por status habilitado/suspenso")
            @RequestParam(required = false) Boolean enabled,
            @Parameter(in = ParameterIn.QUERY, description = "Número da página (inicia em 0)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(in = ParameterIn.QUERY, description = "Quantidade de itens por página")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(in = ParameterIn.QUERY, description = "Campo para ordenação")
            @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(in = ParameterIn.QUERY, description = "Direção da ordenação: asc ou desc")
            @RequestParam(defaultValue = "asc") String direction
    );

    @Operation(
            summary = "Atualizar papel do usuário",
            description = "Atualiza apenas o papel (role) de um usuário pelo seu ID. Requer perfil ADMIN."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Papel atualizado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Papel inválido ou ausente", content = @Content),
            @ApiResponse(responseCode = "401", description = "Token de autenticação ausente, inválido ou expirado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Não autorizado — requer perfil ADMIN", content = @Content),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado", content = @Content)
    })
    ResponseEntity<UserResponse> updateUserRole(
            @Parameter(in = ParameterIn.PATH, description = "ID do usuário", required = true)
            @PathVariable Long id,

            @RequestBody(
                    description = "Novo papel do usuário",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UserRoleUpdateRequest.class))
            )
            UserRoleUpdateRequest request
    );

    @Operation(
            summary = "Atualizar status do usuário",
            description = "Habilita ou suspende o acesso de um usuário pelo seu ID. Requer perfil ADMIN. " +
                    "A suspensão passa a valer no próximo login/refresh; um access token já emitido continua válido até expirar."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Status atualizado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Campo enabled inválido ou ausente", content = @Content),
            @ApiResponse(responseCode = "401", description = "Token de autenticação ausente, inválido ou expirado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Não autorizado — requer perfil ADMIN", content = @Content),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado", content = @Content)
    })
    ResponseEntity<UserResponse> updateUserStatus(
            @Parameter(in = ParameterIn.PATH, description = "ID do usuário", required = true)
            @PathVariable Long id,

            @RequestBody(
                    description = "Novo status do usuário",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UserStatusUpdateRequest.class))
            )
            UserStatusUpdateRequest request
    );
}
