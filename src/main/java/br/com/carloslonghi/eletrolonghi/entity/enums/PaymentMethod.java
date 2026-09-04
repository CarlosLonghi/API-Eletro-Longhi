package br.com.carloslonghi.eletrolonghi.entity.enums;

import lombok.Getter;

@Getter
public enum PaymentMethod {
    CASH("Dinheiro"),
    CARD("Cartão"),
    PIX("PIX"),
    BOLETO("Boleto"),
    MERCADO_PAGO_CHECKOUT("Link de pagamento (Mercado Pago)");

    private final String description;

    PaymentMethod(String description) {
        this.description = description;
    }

}
