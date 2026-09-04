package br.com.carloslonghi.eletrolonghi.exception;

public class PaymentGatewayException extends RuntimeException {

    public PaymentGatewayException(String message) {
        super(message);
    }
}
