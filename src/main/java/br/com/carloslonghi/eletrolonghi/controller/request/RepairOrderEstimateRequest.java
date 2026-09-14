package br.com.carloslonghi.eletrolonghi.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Custo e prazo estimados de uma ordem de reparo")
public record RepairOrderEstimateRequest(
        @Schema(description = "Valor estimado do serviço", example = "350.00")
        @NotNull(message = "RepairOrder 'estimatedCost' is required.")
        @Positive(message = "RepairOrder 'estimatedCost' must be positive.")
        BigDecimal estimatedCost,

        @Schema(description = "Data prevista de conclusão do reparo", example = "2026-09-20")
        @NotNull(message = "RepairOrder 'estimatedCompletionDate' is required.")
        LocalDate estimatedCompletionDate
) {
}
