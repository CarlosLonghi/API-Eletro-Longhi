package br.com.carloslonghi.eletrolonghi.exception;

public class InvalidPaymentCheckoutException extends RuntimeException {

    private InvalidPaymentCheckoutException(String message) {
        super(message);
    }

    public static InvalidPaymentCheckoutException notCheckoutMethod() {
        return new InvalidPaymentCheckoutException(
                "O link de pagamento só está disponível para pagamentos com forma MERCADO_PAGO_CHECKOUT.");
    }

    public static InvalidPaymentCheckoutException notPending() {
        return new InvalidPaymentCheckoutException(
                "O link de pagamento só pode ser gerado para um pagamento pendente.");
    }

    public static InvalidPaymentCheckoutException noCheckoutLink() {
        return new InvalidPaymentCheckoutException(
                "O pagamento não possui link do Checkout Pro para conciliar.");
    }
}
