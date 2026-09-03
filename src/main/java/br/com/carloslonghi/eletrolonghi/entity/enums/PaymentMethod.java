package br.com.carloslonghi.eletrolonghi.entity.enums;

import lombok.Getter;

@Getter
public enum PaymentMethod {
    CASH("Dinheiro"),
    CARD("Cartão"),
    PIX("PIX"),
    BOLETO("Boleto");

    private final String description;

    PaymentMethod(String description) {
        this.description = description;
    }

}
