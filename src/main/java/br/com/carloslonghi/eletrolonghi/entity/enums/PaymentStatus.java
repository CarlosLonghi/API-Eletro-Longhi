package br.com.carloslonghi.eletrolonghi.entity.enums;

import lombok.Getter;

@Getter
public enum PaymentStatus {
    PENDING("Pendente"),
    APPROVED("Aprovado"),
    REJECTED("Recusado"),
    REFUNDED("Estornado"),
    CANCELLED("Cancelado");

    private final String description;

    PaymentStatus(String description) {
        this.description = description;
    }

}
