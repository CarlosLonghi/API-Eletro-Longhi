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
    DEVICE_COLLECTED("Dispositivo coletado");

    private final String description;

    RepairOrderStatus(String description) {
        this.description = description;
    }

    /**
     * Ordem do workflow: {@code true} quando este status vem antes de {@code other}
     * na sequência declarada acima.
     */
    public boolean isBefore(RepairOrderStatus other) {
        return ordinal() < other.ordinal();
    }

}
