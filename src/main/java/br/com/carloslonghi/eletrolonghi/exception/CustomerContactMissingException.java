package br.com.carloslonghi.eletrolonghi.exception;

public class CustomerContactMissingException extends RuntimeException {

    private CustomerContactMissingException(String message) {
        super(message);
    }

    public static CustomerContactMissingException missingEmail(String customerName) {
        return new CustomerContactMissingException(
                "O cliente " + customerName + " não possui e-mail cadastrado.");
    }

    public static CustomerContactMissingException invalidWhatsAppPhone(String customerName) {
        return new CustomerContactMissingException(
                "O cliente " + customerName + " não possui um telefone válido para WhatsApp.");
    }
}
