package br.com.carloslonghi.eletrolonghi.exception;

public class UsernameOrPasswordInvalidException extends RuntimeException {

    public UsernameOrPasswordInvalidException() {
        super("Usuário e(ou) senha inválido(s).");
    }
}
