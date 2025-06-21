package br.com.carloslonghi.eletrolonghi.controller.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

@Schema(description = "Detalhes de um aparelho retornado pela API")
@Builder
public record DeviceResponse(
        @Schema(description = "Identificador único do aparelho")
        Long id,

        @Schema(description = "Modelo do aparelho")
        String model,

        @Schema(description = "Número de série do aparelho", nullable = true)
        String serialNumber,

        @Schema(description = "Informações da marca do aparelho")
        BrandResponse brand,

        @Schema(description = "Lista de acessórios do aparelho")
        List<AccessoryResponse> accessories
) {
}
