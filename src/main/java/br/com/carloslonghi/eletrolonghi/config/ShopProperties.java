package br.com.carloslonghi.eletrolonghi.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Dados da loja usados no cabeçalho do comprovante e prazo da garantia do serviço
 * ({@code warrantyDays}; 0 ou ausente omite a garantia do comprovante).
 * Configurados via {@code shop.*} em {@code application.properties} / {@code .env}.
 */
@ConfigurationProperties(prefix = "shop")
public record ShopProperties(
        String name,
        String document,
        String address,
        String phone,
        String email,
        Integer warrantyDays
) {
}
