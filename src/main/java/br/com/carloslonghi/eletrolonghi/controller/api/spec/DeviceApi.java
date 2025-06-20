package br.com.carloslonghi.eletrolonghi.controller.api.spec;

import br.com.carloslonghi.eletrolonghi.controller.request.DeviceRequest;
import br.com.carloslonghi.eletrolonghi.controller.response.DeviceResponse;
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
        name = "Aparelho",
        description = "Operações para gerenciar aparelhos"
)
@SecurityRequirement(name = "bearerAuth")
public interface DeviceApi {
    @Operation(
            summary = "Cadastrar novo Aparelho",
            description = "Cadastra novo aparelho que pode ser vinculado ao serviço"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Aparelho cadastrado com sucesso",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = DeviceResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Dados da request inválidos", content = @Content),
            @ApiResponse(responseCode = "403", description = "Não autorizado", content = @Content)
    })
    ResponseEntity<DeviceResponse> createDevice(
            @RequestBody(
                    description = "Dados para cadastro de aparelho",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = DeviceRequest.class)
                    )
            )
            DeviceRequest request
    );

    @Operation(
            summary = "Listar todos os aparelhos",
            description = "Retorna a lista completa de aparelhos cadastrados"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista retornada com sucesso",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(
                                    schema = @Schema(implementation = DeviceResponse.class)
                            )
                    )
            ),
            @ApiResponse(responseCode = "403", description = "Não autorizado", content = @Content)
    })
    ResponseEntity<List<DeviceResponse>> getAllDevices();

    @Operation(
            summary = "Buscar aparelho por ID",
            description = "Retorna um único aparelho identificado pelo seu ID"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Aparelho encontrado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = DeviceResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "404", description = "Aparelho não encontrado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Não autorizado", content = @Content)
    })
    ResponseEntity<DeviceResponse> getDeviceById(
            @Parameter(in = ParameterIn.PATH, description = "ID do aparelho", required = true)
            @PathVariable Long id
    );

    @Operation(
            summary = "Listar aparelhos pelo ID da marca",
            description = "Retorna uma lista de aparelhos cadastrados de apenas uma marca, com base no ID da marca"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista retornada com sucesso",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(
                                    schema = @Schema(implementation = DeviceResponse.class)
                            )
                    )
            ),
            @ApiResponse(responseCode = "403", description = "Não autorizado", content = @Content)
    })
    ResponseEntity<List<DeviceResponse>> getDevicesByBrandId(
            @Parameter(in = ParameterIn.QUERY, description = "ID da marca", required = true)
            @PathVariable Long id
    );

    @Operation(
            summary = "Atualizar um Aparelho",
            description = "Atualiza os dados de um aparelho pelo seu ID"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Aparelho atualizado com sucesso",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = DeviceResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Dados da request inválidos", content = @Content),
            @ApiResponse(responseCode = "403", description = "Não autorizado", content = @Content)
    })
    ResponseEntity<DeviceResponse> updateDevice(
            @Parameter(in = ParameterIn.PATH, description = "ID do aparelho", required = true)
            @PathVariable Long id,

            @RequestBody(
                    description = "Dados para atualizar um aparelho",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = DeviceRequest.class)
                    )
            )
            DeviceRequest request
    );

    @Operation(
            summary = "Deletar aparelho por ID",
            description = "Remove um aparelho do sistema pelo seu ID"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Aparelho deletado com sucesso", content = @Content),
            @ApiResponse(responseCode = "404", description = "Aparelho não encontrado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Não autorizado", content = @Content),
            @ApiResponse(responseCode = "409", description = "Aparelho não deletado, pois está relacionado a um serviço", content = @Content)
    })
    ResponseEntity<Void> deleteDeviceById(
            @Parameter(in = ParameterIn.PATH, description = "ID do aparelho", required = true)
            @PathVariable Long id
    );
}
