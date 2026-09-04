package br.com.carloslonghi.eletrolonghi.exception;

public class InvalidPaymentCheckoutException extends RuntimeException {

    public InvalidPaymentCheckoutException(String message) {
        super(message);
    }
}
