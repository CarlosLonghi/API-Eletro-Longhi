package br.com.carloslonghi.eletrolonghi.entity.enums;

import lombok.Getter;

@Getter
public enum RepairOrderStatus {
    AWAITING_EVALUATION("Aguardando avaliação"),
    IN_EVALUATION("Em avaliação"),
    AWAITING_APPROVAL("Aguardando aprovação"),
    APPROVED("Aprovado"),
    AWAITING_PARTS("Aguardando peças"),
    IN_REPAIR("Em reparo"),
    REPAIR_COMPLETED("Reparo concluído"),
    PAYMENT_RECEIVED("Pagamento recebido"),
    DEVICE_COLLECTED("Dispositivo coletado");

    private final String description;

    RepairOrderStatus(String description) {
        this.description = description;
    }

}
