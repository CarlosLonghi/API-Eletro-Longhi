package br.com.carloslonghi.eletrolonghi.controller.api.spec;

import br.com.carloslonghi.eletrolonghi.controller.request.RepairOrderRequest;
import br.com.carloslonghi.eletrolonghi.controller.response.RepairOrderResponse;
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
        name = "Reparo",
        description = "Operações para gerenciar reparos"
)
@SecurityRequirement(name = "bearerAuth")
public interface RepairOrderApi {
    @Operation(
            summary = "Cadastrar novo Reparo",
            description = "Cadastra novo reparo"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Reparo cadastrado com sucesso",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RepairOrderResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Dados da request inválidos", content = @Content),
            @ApiResponse(responseCode = "403", description = "Não autorizado", content = @Content),
            @ApiResponse(responseCode = "409", description = "Aparelho já vinculado a outro Reparo", content = @Content)
    })
    ResponseEntity<RepairOrderResponse> createRepairOrder(
            @RequestBody(
                    description = "Dados para cadastro de reparo",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = RepairOrderRequest.class)
                    )
            )
            RepairOrderRequest request
    );

    @Operation(
            summary = "Listar todos os reparos",
            description = "Retorna a lista completa de reparos cadastrados"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista retornada com sucesso",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(
                                    schema = @Schema(implementation = RepairOrderResponse.class)
                            )
                    )
            ),
            @ApiResponse(responseCode = "403", description = "Não autorizado", content = @Content)
    })
    ResponseEntity<List<RepairOrderResponse>> getAllRepairOrders();

    @Operation(
            summary = "Buscar reparo por ID",
            description = "Retorna um único reparo identificado pelo seu ID"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Reparo encontrado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RepairOrderResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "404", description = "Reparo não encontrado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Não autorizado", content = @Content)
    })
    ResponseEntity<RepairOrderResponse> getRepairOrderById(
            @Parameter(in = ParameterIn.PATH, description = "ID do reparo", required = true)
            @PathVariable Long id
    );

    @Operation(
            summary = "Atualizar um Reparo",
            description = "Atualiza os dados de um reparo pelo seu ID"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Reparo atualizado com sucesso",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RepairOrderResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Dados da request inválidos", content = @Content),
            @ApiResponse(responseCode = "403", description = "Não autorizado", content = @Content),
            @ApiResponse(responseCode = "409", description = "Aparelho já vinculado a outro Reparo", content = @Content)
    })
    ResponseEntity<RepairOrderResponse> updateRepairOrder(
            @Parameter(in = ParameterIn.PATH, description = "ID do reparo", required = true)
            @PathVariable Long id,

            @RequestBody(
                    description = "Dados para atualizar um reparo",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = RepairOrderRequest.class)
                    )
            )
            RepairOrderRequest request
    );

    @Operation(
            summary = "Deletar reparo por ID",
            description = "Remove um reparo do sistema pelo seu ID"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Reparo deletado com sucesso", content = @Content),
            @ApiResponse(responseCode = "404", description = "Reparo não encontrado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Não autorizado", content = @Content)
    })
    ResponseEntity<Void> deleteRepairOrderById(
            @Parameter(in = ParameterIn.PATH, description = "ID do reparo", required = true)
            @PathVariable Long id
    );
}
