package br.com.carloslonghi.eletrolonghi.exception;

import br.com.carloslonghi.eletrolonghi.entity.enums.RepairOrderStatus;

public class InvalidRepairOrderStatusTransitionException extends RuntimeException {

    public InvalidRepairOrderStatusTransitionException(RepairOrderStatus current, RepairOrderStatus next) {
        super("Não é possível mudar o status de " + current + " para " + next + ". " +
              "Só é permitido avançar ou retroceder uma etapa por vez no fluxo.");
    }
}
