package br.com.carloslonghi.eletrolonghi.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
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
        info.description("API para sistema de gerenciamento de serviços");
        info.contact(contact);

        return new OpenAPI().info(info);
    }
}
