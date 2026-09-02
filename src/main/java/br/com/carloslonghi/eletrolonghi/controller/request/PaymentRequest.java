package br.com.carloslonghi.eletrolonghi.controller.request;

import br.com.carloslonghi.eletrolonghi.entity.enums.PaymentMethod;
import br.com.carloslonghi.eletrolonghi.entity.enums.PaymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(description = "Dados para registrar ou atualizar um pagamento")
public record PaymentRequest(
        @Schema(description = "Valor pago", example = "350.00")
        @NotNull(message = "Payment 'amount' is required.")
        @Positive(message = "Payment 'amount' must be positive.")
        BigDecimal amount,

        @Schema(description = "Forma de pagamento", enumAsRef = true)
        @NotNull(message = "Payment 'method' is required.")
        PaymentMethod method,

        @Schema(description = "ID da ordem de reparo paga", example = "1")
        @NotNull(message = "Payment 'repairOrder' is required.")
        Long repairOrder,

        @Schema(description = "Número de parcelas (apenas para cartão); ignorado nas demais formas", example = "3")
        @Min(value = 1, message = "Payment 'installments' must be at least 1.")
        @Max(value = 18, message = "Payment 'installments' must be at most 18.")
        Integer installments,

        @Schema(description = "Situação do pagamento; assume PENDING quando ausente", enumAsRef = true)
        PaymentStatus status,

        @Schema(description = "Observações do pagamento", example = "Entrada do serviço")
        String description,

        @Schema(description = "Nome de quem pagou (impresso no recibo)", example = "João da Silva")
        @Size(max = 100, message = "Payment 'payerName' must be at most 100 characters.")
        String payerName,

        @Schema(description = "CPF/CNPJ de quem pagou (impresso no recibo)", example = "123.456.789-00")
        @Size(max = 20, message = "Payment 'payerDocument' must be at most 20 characters.")
        String payerDocument
) {
}
