package br.com.carloslonghi.eletrolonghi.controller.response;

import br.com.carloslonghi.eletrolonghi.entity.enums.PaymentStatus;
import br.com.carloslonghi.eletrolonghi.entity.enums.RepairOrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "Detalhes de uma ordem de reparo retornada pela API")
public record RepairOrderResponse(
        @Schema(description = "Identificador único da ordem de reparo", example = "1")
        Long id,

        @Schema(description = "Detalhes do serviço solicitado", example = "Tela trincada, não liga")
        String description,

        @Schema(description = "Estado atual do serviço", enumAsRef = true)
        RepairOrderStatus status,

        @Schema(description = "Informações do cliente que solicitou o serviço")
        CustomerResponse customer,

        @Schema(description = "Informações do aparelho do serviço")
        DeviceResponse device,

        @Schema(description = "Situação do pagamento vinculado; null se a ordem ainda não tem pagamento", enumAsRef = true)
        PaymentStatus paymentStatus,

        @Schema(description = "ID do pagamento vinculado; null se a ordem ainda não tem pagamento", example = "12")
        Long paymentId
) {
}
