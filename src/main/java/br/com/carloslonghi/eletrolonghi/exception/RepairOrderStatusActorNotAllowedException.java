package br.com.carloslonghi.eletrolonghi.exception;

import br.com.carloslonghi.eletrolonghi.entity.enums.RepairOrderStatus;
import br.com.carloslonghi.eletrolonghi.entity.enums.Role;

public class RepairOrderStatusActorNotAllowedException extends RuntimeException {

    public RepairOrderStatusActorNotAllowedException(Long repairOrderId, Role actorRole, RepairOrderStatus target) {
        super("O papel " + actorRole + " não pode mudar a ordem de reparo de id " + repairOrderId +
              " para o status " + target + ".");
    }
}
