package br.com.carloslonghi.eletrolonghi.controller.api.spec;

import br.com.carloslonghi.eletrolonghi.controller.request.CustomerRequest;
import br.com.carloslonghi.eletrolonghi.controller.response.CustomerResponse;
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
        name = "Cliente",
        description = "Operações para gerenciar clientes"
)
@SecurityRequirement(name = "bearerAuth")
public interface CustomerApi {
    @Operation(
            summary = "Cadastrar novo Cliente",
            description = "Cadastra novo cliente que pode ser vinculado ao serviço"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Cliente cadastrado com sucesso",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CustomerResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Dados da request inválidos", content = @Content),
            @ApiResponse(responseCode = "403", description = "Não autorizado", content = @Content),
            //  @ApiResponse(responseCode = "409", description = "Cliente já cadastrado", content = @Content)
    })
    ResponseEntity<CustomerResponse> createCustomer(
            @RequestBody(
                    description = "Dados para cadastro de cliente",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = CustomerRequest.class)
                    )
            )
            CustomerRequest request
    );

    @Operation(
            summary = "Listar todos os clientes",
            description = "Retorna a lista completa de clientes cadastrados"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista retornada com sucesso",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(
                                    schema = @Schema(implementation = CustomerResponse.class)
                            )
                    )
            ),
            @ApiResponse(responseCode = "403", description = "Não autorizado", content = @Content)
    })
    ResponseEntity<List<CustomerResponse>> getAllCustomers();

    @Operation(
            summary = "Buscar cliente por ID",
            description = "Retorna um único cliente identificado pelo seu ID"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Cliente encontrado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CustomerResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Não autorizado", content = @Content)
    })
    ResponseEntity<CustomerResponse> getCustomerById(
            @Parameter(in = ParameterIn.PATH, description = "ID do cliente", required = true)
            @PathVariable Long id
    );

    @Operation(
            summary = "Atualizar um Cliente",
            description = "Atualiza os dados de um cliente pelo seu ID"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Cliente atualizado com sucesso",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CustomerResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Dados da request inválidos", content = @Content),
            @ApiResponse(responseCode = "403", description = "Não autorizado", content = @Content),
            //@ApiResponse(responseCode = "409", description = "Cliente já cadastrado", content = @Content)
    })
    ResponseEntity<CustomerResponse> updateCustomer(
            @Parameter(in = ParameterIn.PATH, description = "ID do cliente", required = true)
            @PathVariable Long id,

            @RequestBody(
                    description = "Dados para atualizar um cliente",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = CustomerRequest.class)
                    )
            )
            CustomerRequest request
    );

    @Operation(
            summary = "Deletar cliente por ID",
            description = "Remove um cliente do sistema pelo seu ID"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Cliente deletado com sucesso", content = @Content),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Não autorizado", content = @Content),
            @ApiResponse(responseCode = "409", description = "Cliente não deletado, pois está relacionado a um serviço", content = @Content)
    })
    ResponseEntity<Void> deleteCustomerById(
            @Parameter(in = ParameterIn.PATH, description = "ID do cliente", required = true)
            @PathVariable Long id
    );
}
