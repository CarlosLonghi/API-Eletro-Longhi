# TROUBLESHOOTING

Guia rápido para resolver problemas comuns.

---

## Build & Compile

### Problema: "Cannot find symbol: DeviceMapper" (ou outro mapper)

**Causa:** MapStruct não compilou a implementação.

**Solução:**
```bash
./mvnw clean compile
```

Certifique-se de que o interface tem `@Mapper(componentModel = "spring")`:
```java
@Mapper(componentModel = "spring")
public interface DeviceMapper {
    // ...
}
```

---

### Problema: "maven-compiler-plugin configuration issue"

**Causa:** Configuração do compilador pode estar desatualizada.

**Solução:**
```bash
./mvnw clean install -U
```

Verifique `pom.xml`: `mapstruct-processor` deve estar em `<annotationProcessorPath>`.

---

## Runtime & Database

### Problema: "Connection refused" (localhost:5432)

**Causa:** PostgreSQL não está rodando.

**Solução — Local:**
```bash
# Opção 1: Inicie PostgreSQL (macOS com Homebrew)
brew services start postgresql

# Opção 2: Docker
docker compose up -d

# Opção 3: Verifique application.properties
cat src/main/resources/application.properties | grep datasource
```

### Problema: "database eletro_longhi does not exist"

**Causa:** DB não foi criado.

**Solução:**
```sql
-- Conecte ao PostgreSQL como superuser
psql -U postgres

-- Crie o banco
CREATE DATABASE eletro_longhi;

-- Saia
\q
```

Ou use docker-compose (cria DB automaticamente):
```bash
docker compose up -d
```

---

### Problema: "Flyway migration failed" (V1, V2, etc.)

**Causa:** Migração anterior falhou ou constraint violada.

**Solução:**
```bash
# 1. Limpe o schema (⚠️ dados serão perdidos)
# Conecte ao psql
psql -U postgres -d eletro_longhi
DROP SCHEMA public CASCADE;
CREATE SCHEMA public;
\q

# 2. Recompile e reexecute
./mvnw clean spring-boot:run
```

---

## REST API & Endpoints

### Problema: 403 Forbidden em todo endpoint (exceto /auth/login, /auth/register)

**Causa:** JWT inválido, faltando ou expirado.

**Solução:**

1. Gere um token válido:
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"password123"}'

# Resposta esperada (copie o token):
# {"token":"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."}
```

2. Use o token em requisições:
```bash
curl -X GET http://localhost:8080/api/device \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

3. Se token ainda estiver inválido, verifique `src/main/resources/application.properties`:
```properties
spring.security.secret=sua-chave-jwt-super-secreta-aqui
```

### Problema: 409 Conflict ao criar recurso

**Causa:** Restrição de banco violada (ex: email duplicado, marca existente).

**Solução:**

1. Leia a mensagem de erro (deve estar em `ApplicationControllerAdvice`)
2. Verifique constraints no DB:
```sql
-- Conecte ao psql
psql -U postgres -d eletro_longhi

-- Liste constraints da tabela
\d users  -- mude para a tabela relevante
```

3. Se necessário, limpe dados:
```sql
DELETE FROM devices WHERE brand_id = 1;  -- ⚠️ use com cuidado
```

### Problema: 404 ao acessar /api/device/123 (não encontrado)

**Causa:** ID não existe no BD.

**Solução:**

1. Verifique que o recurso foi criado:
```bash
curl -X GET http://localhost:8080/api/device \
  -H "Authorization: Bearer <seu-token>"
```

2. Use um ID válido da listagem.

---

## Mapper & Data Conversion

### Problema: Campo não mapeado (sempre null na response)

**Causa:** Campo falta no mapper ou anotação `@Mapping` está errada.

**Solução:**

1. Abra o mapper (ex: `mapper/DeviceMapper.java`)
2. Adicione anotação `@Mapping` se precisar renomear:
```java
@Mapper(componentModel = "spring")
public interface DeviceMapper {
    @Mapping(source = "brand_id", target = "brandId")  // ex: se fontes usam snake_case
    DeviceResponse toResponse(DeviceEntity entity);
}
```

3. Recompile:
```bash
./mvnw compile
```

---

## Security & JWT

### Problema: "Invalid signature" ao verificar token

**Causa:** Secret JWT mudou entre build/restart.

**Solução:**

1. Certifique-se de que `spring.security.secret` está definido em `application.properties`
2. Não mude a secret entre requisições (tokens existentes ficam inválidos)
3. Simule novo token:
```bash
./mvnw spring-boot:run  # Restart app com mesma secret
```

### Problema: Token expirado (401 Unauthorized)

**Causa:** Token tem TTL, verifique `config/TokenService.java`

**Solução:**

1. Gere novo token via login:
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"password123"}'
```

2. Se TTL for muito curto, ajuste em `TokenService`:
```java
// Procure por: Claims.builder() ou similar
.withExpiresAt(new Date(System.currentTimeMillis() + 86400000))  // 24 horas
```

---

## Testing

### Problema: Testes falham com "No qualifying bean of type DeviceMapper"

**Causa:** MapStruct não gerou a implementação no contexto de teste.

**Solução:**

1. Adicione `@ExtendWith(SpringExtension.class)` à classe de teste
2. Certifique-se de que mapper está em `@ComponentScan` ou `@SpringBootTest`
3. Recompile:
```bash
./mvnw clean test-compile
```

---

## Performance & Logs

### Ativar logs de debug

**Em `application.properties`:**
```properties
# SQL executado
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# Aplicação
logging.level.br.com.carloslonghi.eletrolonghi=DEBUG
```

**Veja os logs:**
```bash
./mvnw spring-boot:run
# Logs aparecem no console
```

---

## Docker Compose

### Problema: "Cannot connect to Docker daemon"

**Causa:** Docker não está rodando.

**Solução:**
```bash
# Linux
sudo systemctl start docker

# macOS (Docker Desktop)
open /Applications/Docker.app

# Verifique
docker ps
```

### Problema: Container postgres restarts (error, exited)

**Causa:** Configuração inválida ou permissões.

**Solução:**
```bash
# Veja logs
docker compose logs db

# Reinicie limpo
docker compose down -v  # remove volumes
docker compose up -d --build
```

---

## IDE & Editor

### Problema: "Symbol DeviceMapper not resolved" (IntelliJ)

**Causa:** IDE não vê a implementação gerada por MapStruct.

**Solução:**

1. Invalide caches e reinicie:
   - IntelliJ: `File > Invalidate Caches... > Invalidate and Restart`
2. Marque `target/generated-sources/annotations` como "Sources" folder:
   - `File > Project Structure > Modules > [your module] > Sources`
   - Selecione `target/generated-sources/annotations` e marque como "Sources"
3. Recompile via IDE ou terminal:
```bash
./mvnw compile
```

---

## GIT & Version Control

### Problema: "target/ folder too large" antes de commit

**Causa:** `target/` não está em `.gitignore`.

**Solução:**

Crie/atualize `.gitignore`:
```
target/
.idea/
*.iml
.vscode/
```

Remova arquivos já commitados:
```bash
git rm -r --cached target/
git commit -m "Remove target from tracking"
```

---

## Checklist Rápido

Antes de pedir help a agentes ou reportar bug:

- [ ] Rode `./mvnw clean compile`
- [ ] Verifique `application.properties` (DB, JWT secret)
- [ ] Confirme que PostgreSQL está online (`docker compose up -d` ou equivalente)
- [ ] Gerou um token válido via `/auth/login`?
- [ ] Está usando Bearer token no header?
- [ ] Endpoints existem no Swagger UI (`/swagger-ui/index.html`)?
- [ ] Mensagem de erro específica? Pesquise-a aqui ou em `config/ApplicationControllerAdvice.java`

---

## Ainda preso?

1. Procure em `docs/AGENT_PROMPTS.md` por um template relevante
2. Abra um exec-plan: `./scripts/new-exec-plan.sh "Debug: [seu problema]"`
3. Consulte `AGENTS.md` para convenções e estrutura
4. Se tudo falhar, gere um log detalhado e compartilhe com sua equipe


