package br.com.carloslonghi.eletrolonghi.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer"
)
public class SwaggerConfig {

    @Bean
    public OpenAPI getOpenAPI() {

        Contact contact = new Contact();
        contact.setName("Carlos Longhi");
        contact.setEmail("carloslonghi.cl@gmail.com");
        contact.setUrl("https://carloslonghi.com.br/");

        Info info = new Info();
        info.title("API Eletro Longhi");
        info.version("v1");
        info.description("""
                API para sistema de gerenciamento de serviços de uma assistência técnica: \
                cadastro de clientes, aparelhos, marcas, acessórios e acompanhamento de ordens de reparo.

                Autenticação: registre um usuário em POST /auth/register (ou peça a um admin) e faça login em \
                POST /auth/login para obter o token JWT. Clique em "Authorize" acima e informe o token \
                (sem o prefixo "Bearer") para autenticar as chamadas dos demais endpoints.""");
        info.contact(contact);

        return new OpenAPI().info(info);
    }
}
