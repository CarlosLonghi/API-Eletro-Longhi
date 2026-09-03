package br.com.carloslonghi.eletrolonghi.exception;

public class PaymentAlreadyExistsForRepairOrderException extends RuntimeException {

    public PaymentAlreadyExistsForRepairOrderException(Long repairOrderId) {
        super("A ordem de reparo de id " + repairOrderId + " já possui um pagamento registrado. " +
              "Cada ordem admite apenas um pagamento.");
    }
}
