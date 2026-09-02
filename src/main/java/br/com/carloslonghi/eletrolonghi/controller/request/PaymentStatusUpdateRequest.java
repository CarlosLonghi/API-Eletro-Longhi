package br.com.carloslonghi.eletrolonghi.controller.request;

import br.com.carloslonghi.eletrolonghi.entity.enums.PaymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Dados para atualizar a situação de um pagamento")
public record PaymentStatusUpdateRequest(
        @Schema(description = "Nova situação do pagamento", enumAsRef = true)
        @NotNull(message = "O status não pode ser nulo")
        PaymentStatus status
) {
}
