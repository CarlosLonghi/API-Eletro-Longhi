package br.com.carloslonghi.eletrolonghi.exception;

public class InvalidRefreshTokenException extends RuntimeException {

    private InvalidRefreshTokenException(String message) {
        super(message);
    }

    public static InvalidRefreshTokenException notFound() {
        return new InvalidRefreshTokenException("Refresh token inválido.");
    }

    public static InvalidRefreshTokenException revoked() {
        return new InvalidRefreshTokenException("Refresh token revogado.");
    }

    public static InvalidRefreshTokenException expired() {
        return new InvalidRefreshTokenException("Refresh token expirado. Faça login novamente.");
    }

    public static InvalidRefreshTokenException userDisabled() {
        return new InvalidRefreshTokenException("Usuário desativado. Faça login novamente.");
    }
}
