package br.com.carloslonghi.eletrolonghi.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Dados da loja usados no cabeçalho do recibo de pagamento.
 * Configurados via {@code shop.*} em {@code application.properties} / {@code .env}.
 */
@ConfigurationProperties(prefix = "shop")
public record ShopProperties(
        String name,
        String document,
        String address,
        String phone,
        String email
) {
}
