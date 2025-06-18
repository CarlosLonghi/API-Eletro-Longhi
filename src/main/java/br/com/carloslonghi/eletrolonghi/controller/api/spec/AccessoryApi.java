package br.com.carloslonghi.eletrolonghi.controller.api.spec;

import br.com.carloslonghi.eletrolonghi.controller.request.AccessoryRequest;
import br.com.carloslonghi.eletrolonghi.controller.response.AccessoryResponse;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Tag(
        name = "Acessório",
        description = "Operações para gerenciar acessórios dos dispositivos"
)
@SecurityRequirement(name = "bearerAuth")
public interface AccessoryApi {
    @Operation(
            summary = "Cadastrar novo acessório",
            description = "Cria um novo acessório (ex: cabo, controle, etc.)"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Acessório criado com sucesso",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AccessoryResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Dados da request inválidos", content = @Content),
            @ApiResponse(responseCode = "403", description = "Não autorizado", content = @Content),
            @ApiResponse(responseCode = "409", description = "Acessório já cadastrado", content = @Content)
    })
    ResponseEntity<AccessoryResponse> createAccessory(
            @RequestBody(
                    description = "Dados para cadastro do acessório",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = AccessoryRequest.class)
                    )
            )
            AccessoryRequest request
    );

    @Operation(
            summary = "Listar todos os acessórios",
            description = "Retorna a lista completa de acessórios cadastrados"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista retornada com sucesso",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(
                                    schema = @Schema(implementation = AccessoryResponse.class)
                            )
                    )
            ),
            @ApiResponse(responseCode = "403", description = "Não autorizado", content = @Content)
    })
    ResponseEntity<List<AccessoryResponse>> getAllAccessories();

    @Operation(
            summary = "Buscar acessório por ID",
            description = "Retorna um único acessório identificado pelo seu ID"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Acessório encontrado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AccessoryResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "404", description = "Acessório não encontrado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Não autorizado", content = @Content)
    })
    ResponseEntity<AccessoryResponse> getAccessoryById(
            @Parameter(in = ParameterIn.PATH, description = "ID do acessório", required = true)
            @PathVariable Long id
    );

    @Operation(
            summary = "Deletar acessório por ID",
            description = "Remove um acessório do sistema pelo seu ID"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Acessório deletado com sucesso", content = @Content),
            @ApiResponse(responseCode = "404", description = "Acessório não encontrado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Não autorizado", content = @Content)
    })
    ResponseEntity<Void> deleteAccessoryById(
            @Parameter(in = ParameterIn.PATH, description = "ID do acessório", required = true)
            @PathVariable Long id
    );
}
