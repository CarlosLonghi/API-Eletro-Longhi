package br.com.carloslonghi.eletrolonghi.exception;

import br.com.carloslonghi.eletrolonghi.entity.enums.RepairOrderStatus;

public class RepairOrderNotApprovedForPaymentException extends RuntimeException {

    public RepairOrderNotApprovedForPaymentException(Long repairOrderId, RepairOrderStatus currentStatus) {
        super("Não é possível registrar um pagamento para a ordem de reparo de id " + repairOrderId +
              ": o orçamento ainda não foi aprovado (status atual: " +
              (currentStatus == null ? "indefinido" : currentStatus.getDescription()) + ").");
    }
}
