package br.com.carloslonghi.eletrolonghi.controller.api.spec;

import br.com.carloslonghi.eletrolonghi.config.JWTUserData;
import br.com.carloslonghi.eletrolonghi.controller.request.RepairOrderEstimateRequest;
import br.com.carloslonghi.eletrolonghi.controller.request.RepairOrderRequest;
import br.com.carloslonghi.eletrolonghi.controller.request.RepairOrderStatusUpdateRequest;
import br.com.carloslonghi.eletrolonghi.controller.response.RepairOrderResponse;
import br.com.carloslonghi.eletrolonghi.entity.enums.PaymentStatus;
import br.com.carloslonghi.eletrolonghi.entity.enums.RepairOrderStatus;
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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;

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
            @ApiResponse(responseCode = "401", description = "Token de autenticação ausente, inválido ou expirado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Não autorizado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Cliente ou aparelho referenciado não encontrado", content = @Content),
            @ApiResponse(responseCode = "422", description = "Aparelho já possui uma ordem de reparo ativa. A ordem anterior deve estar com status DEVICE_COLLECTED para criar uma nova.", content = @Content)
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
            @ApiResponse(responseCode = "401", description = "Token de autenticação ausente, inválido ou expirado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Não autorizado", content = @Content)
    })
    ResponseEntity<Page<RepairOrderResponse>> getAllRepairOrders(
            @Parameter(in = ParameterIn.QUERY, description = "Filtro por status do reparo", schema = @Schema(implementation = RepairOrderStatus.class))
            @RequestParam(required = false) RepairOrderStatus status,
            @Parameter(in = ParameterIn.QUERY, description = "Filtro pela situação do pagamento vinculado", schema = @Schema(implementation = PaymentStatus.class))
            @RequestParam(required = false) PaymentStatus paymentStatus,
            @Parameter(in = ParameterIn.QUERY, description = "Filtro por ID do cliente")
            @RequestParam(required = false) Long customerId,
            @Parameter(in = ParameterIn.QUERY, description = "Filtro por ID do aparelho")
            @RequestParam(required = false) Long deviceId,
            @Parameter(in = ParameterIn.QUERY, description = "Data inicial de criação (ISO-8601)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdFrom,
            @Parameter(in = ParameterIn.QUERY, description = "Data final de criação (ISO-8601)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdTo,
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
            @ApiResponse(responseCode = "401", description = "Token de autenticação ausente, inválido ou expirado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Não autorizado", content = @Content)
    })
    ResponseEntity<RepairOrderResponse> getRepairOrderById(
            @Parameter(in = ParameterIn.PATH, description = "ID do reparo", required = true)
            @PathVariable Long id
    );

    @Operation(
            summary = "Atualizar um Reparo",
            description = "Atualiza os dados de um reparo (descrição, cliente, aparelho) pelo seu ID. "
                    + "O campo 'status' do corpo é ignorado aqui — só o endpoint PATCH /repair-order/{id}/status "
                    + "muda o status do serviço. Requer perfil ADMIN, GERENTE ou ATENDENTE."
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
            @ApiResponse(responseCode = "401", description = "Token de autenticação ausente, inválido ou expirado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Não autorizado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Reparo, cliente ou aparelho referenciado não encontrado", content = @Content)
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
            summary = "Atualizar status do Reparo",
            description = "Atualiza apenas o status de andamento de um reparo pelo seu ID. É a única forma de "
                    + "mudar o status do serviço. Perfis GERENTE e ADMIN fazem qualquer transição. TECNICO conduz "
                    + "o fluxo de oficina, mas não pode marcar DEVICE_COLLECTED (aparelho retirado pelo cliente). "
                    + "ATENDENTE só atua nessa etapa final, marcando DEVICE_COLLECTED — as demais transições são "
                    + "exclusivas de TECNICO/gestão."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Status atualizado com sucesso",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RepairOrderResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Status inválido ou ausente", content = @Content),
            @ApiResponse(responseCode = "401", description = "Token de autenticação ausente, inválido ou expirado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Não autorizado, ou papel não pode realizar essa transição específica "
                    + "(TECNICO tentando DEVICE_COLLECTED, ou ATENDENTE tentando qualquer transição que não seja DEVICE_COLLECTED)", content = @Content),
            @ApiResponse(responseCode = "404", description = "Reparo não encontrado", content = @Content),
            @ApiResponse(responseCode = "422", description = "Transição de status inválida (mais de uma etapa por vez), tentativa de ir para DEVICE_COLLECTED sem um pagamento aprovado vinculado, ou tentativa de ir para AWAITING_APPROVAL sem custo e prazo estimados definidos (ver PATCH /repair-order/{id}/estimate)", content = @Content)
    })
    ResponseEntity<RepairOrderResponse> updateRepairOrderStatus(
            @Parameter(in = ParameterIn.PATH, description = "ID do reparo", required = true)
            @PathVariable Long id,

            @RequestBody(
                    description = "Novo status do reparo",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = RepairOrderStatusUpdateRequest.class)
                    )
            )
            RepairOrderStatusUpdateRequest request,

            @Parameter(hidden = true)
            @AuthenticationPrincipal JWTUserData jwtUserData
    );

    @Operation(
            summary = "Definir custo e prazo estimados do Reparo",
            description = "Define o custo e a data prevista de conclusão estimados para o reparo, normalmente "
                    + "preenchidos ao final da avaliação técnica. É pré-requisito para a ordem sair de IN_EVALUATION "
                    + "e entrar em AWAITING_APPROVAL (PATCH /repair-order/{id}/status)."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Estimativa atualizada com sucesso",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RepairOrderResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Dados da request inválidos", content = @Content),
            @ApiResponse(responseCode = "401", description = "Token de autenticação ausente, inválido ou expirado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Não autorizado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Reparo não encontrado", content = @Content)
    })
    ResponseEntity<RepairOrderResponse> updateRepairOrderEstimate(
            @Parameter(in = ParameterIn.PATH, description = "ID do reparo", required = true)
            @PathVariable Long id,

            @RequestBody(
                    description = "Custo e prazo estimados do reparo",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = RepairOrderEstimateRequest.class)
                    )
            )
            RepairOrderEstimateRequest request
    );

    @Operation(
            summary = "Deletar reparo por ID",
            description = "Remove um reparo do sistema pelo seu ID. É um soft delete (o registro deixa de aparecer nas consultas, mas é preservado). Requer perfil ADMIN ou GERENTE."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Reparo deletado com sucesso", content = @Content),
            @ApiResponse(responseCode = "404", description = "Reparo não encontrado", content = @Content),
            @ApiResponse(responseCode = "401", description = "Token de autenticação ausente, inválido ou expirado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Não autorizado — requer perfil ADMIN ou GERENTE", content = @Content)
    })
    ResponseEntity<Void> deleteRepairOrderById(
            @Parameter(in = ParameterIn.PATH, description = "ID do reparo", required = true)
            @PathVariable Long id
    );
}
