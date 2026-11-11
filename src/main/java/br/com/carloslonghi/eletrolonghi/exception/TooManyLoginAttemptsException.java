package br.com.carloslonghi.eletrolonghi.exception;

public class TooManyLoginAttemptsException extends RuntimeException {

    public TooManyLoginAttemptsException() {
        super("Muitas tentativas de login inválidas. Tente novamente mais tarde.");
    }
}
