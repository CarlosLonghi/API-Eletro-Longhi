package br.com.carloslonghi.eletrolonghi.exception;

public class RepairOrderNotPaidException extends RuntimeException {

    public RepairOrderNotPaidException(Long repairOrderId) {
        super("A ordem de reparo de id " + repairOrderId + " não pode ser marcada como coletada: " +
              "é necessário um pagamento aprovado vinculado à ordem.");
    }
}
