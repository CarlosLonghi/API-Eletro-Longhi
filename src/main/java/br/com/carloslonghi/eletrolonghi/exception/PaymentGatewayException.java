package br.com.carloslonghi.eletrolonghi.exception;

public class PaymentGatewayException extends RuntimeException {

    public PaymentGatewayException() {
        super("Não foi possível gerar o link de pagamento no Mercado Pago.");
    }
}
