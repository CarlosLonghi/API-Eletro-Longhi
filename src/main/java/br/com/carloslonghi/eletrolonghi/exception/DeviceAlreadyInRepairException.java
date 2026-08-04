package br.com.carloslonghi.eletrolonghi.exception;

public class DeviceAlreadyInRepairException extends RuntimeException {

    public DeviceAlreadyInRepairException(Long deviceId) {
        super("O aparelho de id " + deviceId + " já possui uma ordem de reparo ativa. " +
              "A ordem anterior deve ser concluída (status DEVICE_COLLECTED) antes de criar uma nova.");
    }
}

