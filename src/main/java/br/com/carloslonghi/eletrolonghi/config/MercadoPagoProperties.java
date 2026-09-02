package br.com.carloslonghi.eletrolonghi.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuração do Mercado Pago para a integração futura (maquininha via API Point,
 * confirmação por webhook ou polling). Consumida por {@code client/MercadoPagoClient}.
 * Configurada via {@code mercadopago.*} em {@code application.properties} / {@code .env}.
 */
@ConfigurationProperties(prefix = "mercadopago")
public record MercadoPagoProperties(
        String accessToken,
        String baseUrl,
        String webhookSecret
) {

    public MercadoPagoProperties {
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = "https://api.mercadopago.com";
        }
    }
}
