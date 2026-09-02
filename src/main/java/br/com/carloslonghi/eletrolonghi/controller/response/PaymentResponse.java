package br.com.carloslonghi.eletrolonghi.controller.response;

import br.com.carloslonghi.eletrolonghi.entity.enums.PaymentMethod;
import br.com.carloslonghi.eletrolonghi.entity.enums.PaymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@Schema(description = "Detalhes de um pagamento retornado pela API")
public record PaymentResponse(
        @Schema(description = "Identificador único do pagamento", example = "1")
        Long id,

        @Schema(description = "Valor pago", example = "350.00")
        BigDecimal amount,

        @Schema(description = "Forma de pagamento", enumAsRef = true)
        PaymentMethod method,

        @Schema(description = "Situação do pagamento", enumAsRef = true)
        PaymentStatus status,

        @Schema(description = "Número de parcelas", example = "3")
        Integer installments,

        @Schema(description = "Observações do pagamento", example = "Entrada do serviço")
        String description,

        @Schema(description = "Nome de quem pagou", example = "João da Silva")
        String payerName,

        @Schema(description = "CPF/CNPJ de quem pagou", example = "123.456.789-00")
        String payerDocument,

        @Schema(description = "Referência externa no gateway de pagamento (uso futuro)")
        String externalReference,

        @Schema(description = "ID do pagamento no gateway (uso futuro)")
        String gatewayPaymentId,

        @Schema(description = "ID da ordem de reparo paga", example = "1")
        Long repairOrderId,

        @Schema(description = "Momento em que o pagamento foi aprovado")
        LocalDateTime paidAt,

        @Schema(description = "Momento de criação do registro")
        LocalDateTime createdAt
) {
}
