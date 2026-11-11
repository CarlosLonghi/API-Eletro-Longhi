package br.com.carloslonghi.eletrolonghi.exception;

public class ReceiptDeliveryException extends RuntimeException {

    public ReceiptDeliveryException(Throwable cause) {
        super("Não foi possível enviar o comprovante por e-mail.", cause);
    }
}
