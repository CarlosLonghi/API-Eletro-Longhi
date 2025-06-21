package br.com.carloslonghi.eletrolonghi.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Schema(description = "Dados para criar ou atualizar um aparelho")
public record DeviceRequest(
        @Schema(description = "Modelo do aparelho")
        @NotBlank(message = "O modelo do aparelho é obrigatório.")
        String model,

        @Schema(description = "Número de série do aparelho")
        String serialNumber,

        @Schema(description = "ID da marca do aparelho")
        @NotNull(message = "A marca do aparelho é obrigatória.")
        Long brand,

        @Schema(description = "Lista de IDs dos acessórios do aparelho")
        @NotNull(message = "A lista de acessórios do aparelho é obrigatória.")
        List<Long> accessories
) {
}
