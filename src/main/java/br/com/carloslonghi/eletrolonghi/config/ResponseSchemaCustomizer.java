package br.com.carloslonghi.eletrolonghi.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.JsonSchema;
import io.swagger.v3.oas.models.media.Schema;
import org.jspecify.annotations.Nullable;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.stereotype.Component;

import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Torna explícito no contrato OpenAPI o que cada resposta sempre traz. Por padrão o springdoc não marca
 * nenhuma propriedade de resposta como {@code required}, e o gerador de tipos do app desktop
 * (openapi-typescript) trata tudo como opcional. Este customizer percorre os schemas {@code *Response}
 * do pacote {@code controller.response} e:
 * <ul>
 *     <li>marca como {@code required} todo componente do record (o Jackson da API serializa nulls, então a
 *     chave sempre vem na resposta);</li>
 *     <li>aceita {@code null} nos componentes com {@code @Nullable} (JSpecify): o {@code nullable = true}
 *     do {@code @Schema} é descartado na saída OpenAPI 3.1, então o tipo {@code "null"} é adicionado aqui
 *     (ou {@code oneOf [$ref, null]} quando a propriedade é uma referência, como um enum).</li>
 * </ul>
 */
@Component
public class ResponseSchemaCustomizer implements OpenApiCustomizer {

    static final String RESPONSE_PACKAGE = "br.com.carloslonghi.eletrolonghi.controller.response";

    @Override
    public void customise(OpenAPI openApi) {
        if (openApi.getComponents() == null || openApi.getComponents().getSchemas() == null) {
            return;
        }
        openApi.getComponents().getSchemas().forEach((name, schema) -> {
            if (name.endsWith("Response") && schema.getProperties() != null) {
                responseRecord(name).ifPresent(type -> apply(type, schema));
            }
        });
    }

    private static Optional<Class<?>> responseRecord(String schemaName) {
        try {
            Class<?> type = Class.forName(RESPONSE_PACKAGE + "." + schemaName);
            return type.isRecord() ? Optional.of(type) : Optional.empty();
        } catch (ClassNotFoundException e) {
            return Optional.empty();
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void apply(Class<?> type, Schema<?> schema) {
        Map<String, Schema> properties = schema.getProperties();
        List<String> required = new ArrayList<>();
        for (RecordComponent component : type.getRecordComponents()) {
            Schema property = properties.get(component.getName());
            if (property == null) {
                continue;
            }
            // O Jackson serializa nulls, então a chave sempre vem; @Nullable só libera o valor null
            required.add(component.getName());
            if (isNullable(component)) {
                properties.put(component.getName(), allowNull(property));
            }
        }
        schema.setRequired(required.stream().sorted().toList());
    }

    private static boolean isNullable(RecordComponent component) {
        return component.getAnnotatedType().isAnnotationPresent(Nullable.class)
                || component.isAnnotationPresent(Nullable.class);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Schema allowNull(Schema property) {
        if (property.get$ref() == null) {
            property.addType("null");
            return property;
        }
        // Referência (enum/objeto): $ref não admite irmãos de tipo, então vira oneOf [ref, null]
        Schema wrapper = new JsonSchema();
        wrapper.setDescription(property.getDescription());
        wrapper.setOneOf(List.of(new JsonSchema().$ref(property.get$ref()), new JsonSchema().types(Set.of("null"))));
        return wrapper;
    }
}
