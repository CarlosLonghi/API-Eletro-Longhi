package br.com.carloslonghi.eletrolonghi.contract;

import br.com.carloslonghi.eletrolonghi.repository.support.AbstractPostgresIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Snapshot do contrato OpenAPI em {@code openapi/openapi.json}, consumido pelo app desktop para gerar os
 * tipos TypeScript e pelo CI (oasdiff) para barrar breaking changes. Toda mudança de contrato precisa
 * aparecer nesse arquivo no mesmo PR: se o teste falhar, regrave o snapshot com
 * {@code ./mvnw test -Dtest=OpenApiContractTest -Dopenapi.update=true} e revise o diff.
 */
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                // Saída determinística: chaves ordenadas e indentadas, para o diff do snapshot ser legível
                "springdoc.writer-with-order-by-keys=true",
                "springdoc.writer-with-default-pretty-printer=true"
        })
class OpenApiContractTest extends AbstractPostgresIntegrationTest {

    private static final Path SNAPSHOT = Path.of("openapi", "openapi.json");

    @LocalServerPort
    private int port;

    @Test
    void contractSnapshotShouldMatchGeneratedSpec() throws IOException {
        String generated = RestClient.create().get()
                .uri("http://localhost:" + port + "/api/api-docs")
                .retrieve()
                .body(String.class);

        // A URL do servidor carrega a porta aleatória do teste; fixa a porta padrão da API
        String spec = generated
                .replace("http://localhost:" + port, "http://localhost:8080")
                .replace("\r\n", "\n")
                .strip() + "\n";

        if (Boolean.getBoolean("openapi.update") || Files.notExists(SNAPSHOT)) {
            Files.createDirectories(SNAPSHOT.getParent());
            Files.writeString(SNAPSHOT, spec, StandardCharsets.UTF_8);
        }

        assertThat(Files.readString(SNAPSHOT, StandardCharsets.UTF_8))
                .as("Contrato OpenAPI mudou. Rode ./mvnw test -Dtest=OpenApiContractTest -Dopenapi.update=true "
                        + "e commite openapi/openapi.json junto com a mudança")
                .isEqualTo(spec);
    }
}
