package br.com.carloslonghi.eletrolonghi.exception;

public class AccountNotActivatedException extends RuntimeException {

    public AccountNotActivatedException() {
        super("Conta aguardando ativação por um administrador.");
    }
}
