package br.com.carloslonghi.eletrolonghi.controller.api.spec;

import br.com.carloslonghi.eletrolonghi.controller.request.PaymentRequest;
import br.com.carloslonghi.eletrolonghi.controller.request.PaymentStatusUpdateRequest;
import br.com.carloslonghi.eletrolonghi.controller.response.PaymentResponse;
import br.com.carloslonghi.eletrolonghi.entity.enums.PaymentMethod;
import br.com.carloslonghi.eletrolonghi.entity.enums.PaymentStatus;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;

@Tag(
        name = "Pagamento",
        description = "Operações para gerenciar os pagamentos das ordens de reparo"
)
@SecurityRequirement(name = "bearerAuth")
public interface PaymentApi {

    @Operation(
            summary = "Registrar novo pagamento",
            description = "Registra o pagamento de uma ordem de reparo. Cada ordem admite apenas um pagamento."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Pagamento registrado com sucesso",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PaymentResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Dados da request inválidos", content = @Content),
            @ApiResponse(responseCode = "401", description = "Token de autenticação ausente, inválido ou expirado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Não autorizado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Ordem de reparo referenciada não encontrada", content = @Content),
            @ApiResponse(responseCode = "422", description = "A ordem de reparo já possui um pagamento registrado", content = @Content)
    })
    ResponseEntity<PaymentResponse> createPayment(
            @RequestBody(
                    description = "Dados para registrar o pagamento",
                    required = true,
                    content = @Content(schema = @Schema(implementation = PaymentRequest.class))
            )
            PaymentRequest request
    );

    @Operation(
            summary = "Listar pagamentos",
            description = "Retorna os pagamentos cadastrados, paginados e filtráveis"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista retornada com sucesso",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = PaymentResponse.class))
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Token de autenticação ausente, inválido ou expirado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Não autorizado", content = @Content)
    })
    ResponseEntity<Page<PaymentResponse>> getAllPayments(
            @Parameter(in = ParameterIn.QUERY, description = "Filtro por situação do pagamento", schema = @Schema(implementation = PaymentStatus.class))
            @RequestParam(required = false) PaymentStatus status,
            @Parameter(in = ParameterIn.QUERY, description = "Filtro por forma de pagamento", schema = @Schema(implementation = PaymentMethod.class))
            @RequestParam(required = false) PaymentMethod method,
            @Parameter(in = ParameterIn.QUERY, description = "Filtro por ID da ordem de reparo")
            @RequestParam(required = false) Long repairOrderId,
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
            summary = "Buscar pagamento por ID",
            description = "Retorna um único pagamento identificado pelo seu ID"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Pagamento encontrado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PaymentResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "404", description = "Pagamento não encontrado", content = @Content),
            @ApiResponse(responseCode = "401", description = "Token de autenticação ausente, inválido ou expirado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Não autorizado", content = @Content)
    })
    ResponseEntity<PaymentResponse> getPaymentById(
            @Parameter(in = ParameterIn.PATH, description = "ID do pagamento", required = true)
            @PathVariable Long id
    );

    @Operation(
            summary = "Atualizar um pagamento",
            description = "Atualiza os dados de um pagamento pelo seu ID (não altera a ordem vinculada)"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Pagamento atualizado com sucesso",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PaymentResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Dados da request inválidos", content = @Content),
            @ApiResponse(responseCode = "401", description = "Token de autenticação ausente, inválido ou expirado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Não autorizado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Pagamento não encontrado", content = @Content)
    })
    ResponseEntity<PaymentResponse> updatePayment(
            @Parameter(in = ParameterIn.PATH, description = "ID do pagamento", required = true)
            @PathVariable Long id,

            @RequestBody(
                    description = "Dados para atualizar o pagamento",
                    required = true,
                    content = @Content(schema = @Schema(implementation = PaymentRequest.class))
            )
            PaymentRequest request
    );

    @Operation(
            summary = "Atualizar a situação de um pagamento",
            description = "Atualiza apenas a situação do pagamento. Ao mudar para APPROVED, a ordem de "
                    + "reparo vinculada avança de REPAIR_COMPLETED para PAYMENT_RECEIVED automaticamente."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Situação atualizada com sucesso",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PaymentResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Situação inválida ou ausente", content = @Content),
            @ApiResponse(responseCode = "401", description = "Token de autenticação ausente, inválido ou expirado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Não autorizado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Pagamento não encontrado", content = @Content)
    })
    ResponseEntity<PaymentResponse> updatePaymentStatus(
            @Parameter(in = ParameterIn.PATH, description = "ID do pagamento", required = true)
            @PathVariable Long id,

            @RequestBody(
                    description = "Nova situação do pagamento",
                    required = true,
                    content = @Content(schema = @Schema(implementation = PaymentStatusUpdateRequest.class))
            )
            PaymentStatusUpdateRequest request
    );

    @Operation(
            summary = "Emitir o recibo do pagamento",
            description = "Retorna o comprovante não-fiscal do pagamento em PDF"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Recibo gerado com sucesso",
                    content = @Content(mediaType = "application/pdf")
            ),
            @ApiResponse(responseCode = "404", description = "Pagamento não encontrado", content = @Content),
            @ApiResponse(responseCode = "401", description = "Token de autenticação ausente, inválido ou expirado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Não autorizado", content = @Content)
    })
    ResponseEntity<byte[]> getPaymentReceipt(
            @Parameter(in = ParameterIn.PATH, description = "ID do pagamento", required = true)
            @PathVariable Long id
    );

    @Operation(
            summary = "Deletar pagamento por ID",
            description = "Remove um pagamento do sistema pelo seu ID. Requer perfil ADMIN."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Pagamento deletado com sucesso", content = @Content),
            @ApiResponse(responseCode = "404", description = "Pagamento não encontrado", content = @Content),
            @ApiResponse(responseCode = "401", description = "Token de autenticação ausente, inválido ou expirado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Não autorizado — requer perfil ADMIN", content = @Content)
    })
    ResponseEntity<Void> deletePaymentById(
            @Parameter(in = ParameterIn.PATH, description = "ID do pagamento", required = true)
            @PathVariable Long id
    );
}
