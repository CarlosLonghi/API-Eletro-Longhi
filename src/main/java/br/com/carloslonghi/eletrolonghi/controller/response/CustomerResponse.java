package br.com.carloslonghi.eletrolonghi.controller.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import org.jspecify.annotations.Nullable;

@Schema(description = "Detalhes de um cliente retornado pela API")
@Builder
public record CustomerResponse(
        @Schema(description = "Identificador único do cliente", example = "1")
        Long id,

        @Schema(description = "Nome do cliente", example = "João da Silva")
        String name,

        @Schema(description = "Telefone do cliente", example = "(11) 91234-5678")
        String phone,

        @Schema(description = "E-mail do cliente", example = "joao.silva@email.com")
        @Nullable String email
) {
}
