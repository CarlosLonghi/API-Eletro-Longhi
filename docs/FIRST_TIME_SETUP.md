# FIRST_TIME_SETUP

Guia passo-a-passo para colocar o projeto rodando na primeira vez.

---

## Pré-requisitos

Verifique se você tem:

- **Java 21+** instalado:
  ```bash
  java -version
  ```
  Esperado: `openjdk 21.x.x` ou similar

- **Maven 3.9+** (ou use o bundled mvnw):
  ```bash
  ./mvnw -version
  ```

- **PostgreSQL 14+** ou **Docker**:
  ```bash
  psql --version
  # ou
  docker --version && docker compose --version
  ```

- **Git** instalado:
  ```bash
  git --version
  ```

---

## Opção 1: Rodando com Docker Compose (Recomendado)

**Pré-requisitos:** Docker + Docker Compose

### Passo 1: Clone o repositório

```bash
git clone https://github.com/CarlosLonghi/API-Eletro-Longhi.git
cd API-Eletro-Longhi
```

### Passo 2: Inicie o PostgreSQL via Docker

```bash
docker compose up -d
```

Isto vai:
- Criar container PostgreSQL
- Iniciar banco em localhost:5432
- Credenciais padrão: `postgres:postgres` (ver `docker-compose.yml`)

**Verifique se está rodando:**
```bash
docker compose ps
# Expected output:
# NAME        STATUS
# db          Up X minutes
```

### Passo 3: Configure `application.properties`

Abra `src/main/resources/application.properties` e certifique-se de que tem:

```properties
# Database (Docker Compose)
spring.datasource.url=jdbc:postgresql://localhost:5432/eletro_longhi
spring.datasource.username=postgres
spring.datasource.password=postgres

# JWT Secret (mude em produção!)
spring.security.secret=sua-chave-secreta-super-segura

# Flyway
spring.flyway.baseline-on-migrate=true

# OpenAPI/Swagger
springdoc.api-docs.path=/api/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
```

### Passo 4: Compile o projeto

```bash
./mvnw clean compile
```

Isto vai:
- Baixar todas as dependências Maven
- Compilar o código Java
- **Gerar implementations de MapStruct** (importante!)

Esperado: `BUILD SUCCESS`

### Passo 5: Inicie a aplicação

```bash
./mvnw spring-boot:run
```

Esperado na saída:
```
...
Tomcat started on port(s): 8080 (http)
...
Started EletrolonghiApplication in X.XXX seconds (JVM running for X.XXX)
```

### Passo 6: Teste se está funcionando

Em outro terminal:

```bash
# 1. Registre um usuário
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"senha123"}'

# Expected: 201 Created, retorna token ou user data

# 2. Faça login
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"senha123"}'

# Expected: 200 OK, retorna {"token":"eyJhbGc..."}

# 3. Use o token para listar marcas (use o token da resposta anterior)
TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
curl -X GET http://localhost:8080/api/brand \
  -H "Authorization: Bearer $TOKEN"

# Expected: 200 OK, retorna lista (vazia no início)
```

### Passo 7 (Opcional): Veja Swagger UI

Abra no navegador:
```
http://localhost:8080/swagger-ui/index.html
```

Aqui você pode testar todos os endpoints via UI.

---

## Opção 2: Rodando Localmente (sem Docker)

**Pré-requisitos:** Java 21, Maven, PostgreSQL local

### Passo 1-3: Mesmo que acima (clone + configure properties)

```bash
git clone https://github.com/CarlosLonghi/API-Eletro-Longhi.git
cd API-Eletro-Longhi
```

### Passo 4: Crie o banco PostgreSQL

Conecte como superuser (ex: via pgAdmin ou CLI):

```bash
psql -U postgres
```

Depois:

```sql
CREATE DATABASE eletro_longhi;
\q
```

Ou via comando one-liner:

```bash
psql -U postgres -c "CREATE DATABASE eletro_longhi;"
```

### Passo 5: Configure `application.properties`

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/eletro_longhi
spring.datasource.username=postgres
spring.datasource.password=SUA_SENHA_POSTGRES

spring.security.secret=sua-chave-secreta-aqui
```

### Passo 6: Compile

```bash
./mvnw clean compile
```

### Passo 7: Inicie

```bash
./mvnw spring-boot:run
```

Resto é igual à Opção 1.

---

## Troubleshooting Setup

### Erro: "Cannot connect to Docker daemon"

**Solução:**

```bash
# Linux
sudo systemctl start docker

# macOS
open /Applications/Docker.app

# Windows
# Abra Docker Desktop manualmente
```

### Erro: "database eletro_longhi does not exist"

**Com Docker Compose:**
```bash
# A db é criada automaticamente, mas se falhar:
docker compose down -v
docker compose up -d
```

**Localmente:**
```bash
psql -U postgres -c "CREATE DATABASE eletro_longhi;"
```

### Erro: "Cannot find symbol: DeviceMapper"

**Solução:**
```bash
./mvnw clean compile
```

Se persistir, invalide cache da IDE (IntelliJ):
- `File > Invalidate Caches... > Invalidate and Restart`

### Erro: "Port 8080 already in use"

```bash
# Mude a porta em application.properties
echo "server.port=8081" >> src/main/resources/application.properties

# Ou mate o processo existente
lsof -i :8080 | grep LISTEN | awk '{print $2}' | xargs kill -9
```

### Erro: "Flyway migration failed"

```bash
# Limpe schema (⚠️ perde dados)
psql -U postgres -d eletro_longhi
DROP SCHEMA public CASCADE;
CREATE SCHEMA public;
\q

# Tente novamente
./mvnw spring-boot:run
```

---

## Verificação Final

Rode este checklist após setup:

- [ ] `./mvnw compile` → BUILD SUCCESS
- [ ] `docker compose ps` ou `psql` → DB conectado
- [ ] `./mvnw spring-boot:run` → Tomcat started
- [ ] `http://localhost:8080/swagger-ui.html` → Abre Swagger UI
- [ ] `/auth/login` com credenciais → retorna token
- [ ] `/api/brand` com token → retorna status 200

Se tudo passou ✅, você está pronto!

---

## Próximos Passos

1. Leia `docs/QUICK_START_FOR_AGENTS.md` (guia para agentes/IA)
2. Explore exemplo em `src/main/java/.../controller/DeviceController.java`
3. Veja `AGENTS.md` para convenções do projeto
4. Crie sua primeira branch: `git checkout -b feature/seu-nome`

---

## FAQ Rápido

### P: Como mudo o JWT secret?

R: Em `src/main/resources/application.properties`:
```properties
spring.security.secret=novo-segredo-aqui
```

Tokens existentes ficam inválidos após mudança.

### P: Como adiciono uma nova coluna à tabela?

R: Crie migração em `src/main/resources/db/migration/V8__seu_titulo.sql` (próxima numeração), execute `./mvnw spring-boot:run` para aplicar.

### P: Como rodo testes?

R: `./mvnw test` (note: testes são mínimos no projeto hoje).

### P: Como faço build do JAR?

R: `./mvnw -q -DskipTests package` → gera `target/eletrolonghi-0.0.1-SNAPSHOT.jar`.

### P: Como debugo no IDE?

R: 
- IntelliJ: `Run > Debug` (ou `Shift+F9`)
- VS Code: Instale Extension Pack for Java, setup launch config
- CLI: `./mvnw spring-boot:run -Dspring-boot.run.arguments="--debug"`

---

## Estrutura Rápida (depois do setup)

```
API-Eletro-Longhi/
├── AGENTS.md                 ← Leia isto para convenções
├── ARCHITECTURE.md           ← Visão geral
├── docs/
│   ├── QUICK_START_FOR_AGENTS.md  ← Guia para agentes/IA
│   ├── STRUCTURE.md          ← Mapa detalhado de pastas
│   └── ...
├── src/main/java/.../
│   ├── controller/           ← Endpoints HTTP
│   ├── service/              ← Lógica
│   ├── repository/           ← Acesso BD
│   ├── mapper/               ← DTO ↔ Entity
│   └── config/               ← Segurança, Swagger
└── src/main/resources/
    ├── application.properties ← Configuração
    └── db/migration/         ← Scripts BD
```

---

## Suporte

Preso? Consulte:

- `docs/TROUBLESHOOTING.md` — Problemas comuns + soluções
- `docs/QUICK_START_FOR_AGENTS.md` — Resumo de 15 minutos
- `AGENTS.md` — Convenções do projeto
- GitHub Issues — Se houver issue pública, comente lá


