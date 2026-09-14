package br.com.carloslonghi.eletrolonghi.exception;

public class RepairOrderMissingEstimateException extends RuntimeException {

    public RepairOrderMissingEstimateException(Long repairOrderId) {
        super("Não é possível aprovar a ordem de reparo de id " + repairOrderId +
              ": defina o custo estimado e a data prevista de conclusão antes de aprovar.");
    }
}
