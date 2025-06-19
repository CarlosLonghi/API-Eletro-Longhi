package br.com.carloslonghi.eletrolonghi.controller.api.spec;

import br.com.carloslonghi.eletrolonghi.controller.request.BrandRequest;
import br.com.carloslonghi.eletrolonghi.controller.response.BrandResponse;
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
        name = "Marca",
        description = "Operações para gerenciar marcas dos dispositivos"
)
@SecurityRequirement(name = "bearerAuth")
public interface BrandApi {
    @Operation(
            summary = "Cadastrar nova marca",
            description = "Cria uma nova marca (ex: LG, Samsung, etc.)"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Marca criada com sucesso",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BrandResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Dados da request inválidos", content = @Content),
            @ApiResponse(responseCode = "403", description = "Não autorizado", content = @Content),
            @ApiResponse(responseCode = "409", description = "Marca já cadastrada", content = @Content)
    })
    ResponseEntity<BrandResponse> createBrand(
            @RequestBody(
                    description = "Dados para cadastro da marca",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = BrandRequest.class)
                    )
            )
            BrandRequest request
    );

    @Operation(
            summary = "Listar todas as marcas",
            description = "Retorna a lista completa de marcas cadastradas"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista retornada com sucesso",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(
                                    schema = @Schema(implementation = BrandResponse.class)
                            )
                    )
            ),
            @ApiResponse(responseCode = "403", description = "Não autorizado", content = @Content)
    })
    ResponseEntity<List<BrandResponse>> getAllBrands();

    @Operation(
            summary = "Buscar marca por ID",
            description = "Retorna uma única marca identificada pelo seu ID"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Marca encontrada",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BrandResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "404", description = "Marca não encontrada", content = @Content),
            @ApiResponse(responseCode = "403", description = "Não autorizado", content = @Content)
    })
    ResponseEntity<BrandResponse> getBrandById(
            @Parameter(in = ParameterIn.PATH, description = "ID da marca", required = true)
            @PathVariable Long id
    );

    @Operation(
            summary = "Deletar marca por ID",
            description = "Remove uma marca do sistema pelo seu ID"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Marca deletada com sucesso", content = @Content),
            @ApiResponse(responseCode = "404", description = "Marca não encontrada", content = @Content),
            @ApiResponse(responseCode = "403", description = "Não autorizado", content = @Content),
            @ApiResponse(responseCode = "409", description = "Marca não deletada, pois está relacionada a um aparelho", content = @Content)
    })
    ResponseEntity<Void> deleteBrandById(
            @Parameter(in = ParameterIn.PATH, description = "ID da marca", required = true)
            @PathVariable Long id
    );
}
