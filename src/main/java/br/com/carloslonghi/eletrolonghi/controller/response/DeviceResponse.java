package br.com.carloslonghi.eletrolonghi.controller.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

@Schema(description = "Detalhes de um aparelho retornado pela API")
@Builder
public record DeviceResponse(
        @Schema(description = "Identificador único do aparelho", example = "1")
        Long id,

        @Schema(description = "Modelo do aparelho", example = "Galaxy S23")
        String model,

        @Schema(description = "Número de série do aparelho", example = "SN-2024-00123", nullable = true)
        String serialNumber,

        @Schema(description = "Informações da marca do aparelho")
        BrandResponse brand,

        @Schema(description = "Lista de acessórios do aparelho")
        List<AccessoryResponse> accessories
) {
}
