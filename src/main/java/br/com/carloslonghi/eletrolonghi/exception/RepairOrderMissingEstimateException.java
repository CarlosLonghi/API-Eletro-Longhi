package br.com.carloslonghi.eletrolonghi.exception;

public class RepairOrderMissingEstimateException extends RuntimeException {

    public RepairOrderMissingEstimateException(Long repairOrderId) {
        super("Não é possível mover a ordem de reparo de id " + repairOrderId +
              " para aguardando aprovação: defina o custo estimado e a data prevista de conclusão.");
    }
}
