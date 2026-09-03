# Eletro Longhi API

[![CI](https://github.com/CarlosLonghi/API-Eletro-Longhi/actions/workflows/ci.yml/badge.svg)](https://github.com/CarlosLonghi/API-Eletro-Longhi/actions/workflows/ci.yml)

### Descrição do Projeto

A **Eletro Longhi API** é um backend em Java (Spring Boot) para gerenciar todo o fluxo de conserto de equipamentos em uma eletrônica: o cliente traz o aparelho, a equipe abre uma ordem de reparo e a acompanha por um workflow de status até a retirada.

Funcionalidades implementadas:

* **Autenticação JWT** (Bearer) com registro, login, **refresh token com rotação**, logout e **proteção contra brute-force** no login (bloqueio temporário após N tentativas).
* **Perfis de acesso** `ADMIN` / `USER` com autorização por rota (Spring Security) — operações destrutivas e a gestão de usuários são exclusivas de `ADMIN`.
* **Fluxo de ativação de conta**: todo usuário auto-registrado nasce desativado (`enabled=false`) e só consegue logar depois que um `ADMIN` ativa a conta e define o perfil.
* **Bootstrap de ADMIN** na primeira inicialização (conta administrativa criada a partir de variáveis de ambiente).
* **Gestão de usuários** (`/user`) — listagem paginada/filtrável e alteração de perfil e de status (ativar/suspender) por um `ADMIN`.
* CRUD de **Marcas** (`/brand`).
* CRUD de **Clientes** (`/customer`) com listagem paginada e filtros.
* CRUD de **Aparelhos** (`/device`) com listagem paginada, filtro por marca/modelo e busca por número de série.
* CRUD de **Acessórios** (`/accessory`).
* CRUD de **Ordens de Reparo** (`/repair-order`) com listagem paginada, filtros (status, cliente, aparelho, intervalo de criação) e **endpoint dedicado de transição de status** (`PATCH /repair-order/{id}/status`), com o fluxo validado (só é permitido avançar/retroceder uma etapa por vez).
* **Regra de negócio de ciclo do aparelho**: um aparelho só pode receber uma nova ordem de reparo depois que a anterior chegou ao status `DEVICE_COLLECTED` (violação → 422).
* CRUD de **Pagamentos** (`/payment`) — um pagamento por ordem de reparo (dinheiro, cartão à vista/parcelado, PIX ou boleto), listagem paginada/filtrável, transição de situação por `PATCH /payment/{id}/status` e **avanço automático** da ordem para `PAYMENT_RECEIVED` ao aprovar o pagamento.
* **Recibo de pagamento em PDF** (`GET /payment/{id}/receipt`) — comprovante não-fiscal com os dados da loja (`shop.*`). Integração com o gateway do Mercado Pago (maquininha via API Point) está preparada como esqueleto para quando a aplicação for hospedada.
* **CORS por allowlist** de origens (front-end web / renderer Electron).
* **Controle de integridade**: tratamento global de conflitos (409), validações (`@Valid` → 400), regras de negócio (→ 422) e 401 explícito para token ausente/inválido.
* **CI no GitHub Actions** rodando a suíte de testes em todo push/PR para `main`, com merge bloqueado enquanto os testes não passam.
* Documentação **OpenAPI/Swagger** em `/swagger-ui/index.html`.

---

### Tecnologias Utilizadas

| Tecnologia | Versão |
|---|---|
| Java | 21 |
| Spring Boot (Web, Security, Data JPA, Validation) | 4.1.0 |
| PostgreSQL | 16 |
| Flyway | (gerenciado pelo Spring Boot) |
| MapStruct | 1.6.3 |
| Springdoc OpenAPI (Swagger UI) | 3.0.0 |
| Auth0 Java JWT | 4.4.0 |
| OpenPDF (recibo em PDF) | 2.0.3 |
| Lombok | (gerenciado pelo Spring Boot) |
| Testcontainers (PostgreSQL) | 1.21.4 |
| JaCoCo (Code Coverage) | 0.8.12 |
| Docker & Docker Compose | — |
| GitHub Actions (CI) | — |

---

### Organização do Código

```text
src/main/java/br/com/carloslonghi/eletrolonghi/
├── config/           # Security, JWT (TokenService/SecurityFilter), CORS, Swagger,
│                     #   ControllerAdvice global, AdminUserSeeder e @ConfigurationProperties
│                     #   (ShopProperties, MercadoPagoProperties)
├── client/           # MercadoPagoClient (esqueleto do gateway) + DTOs do gateway
├── controller/       # Controllers REST (implementam interfaces *Api)
│   ├── api/spec/     # Interfaces de contrato OpenAPI (@Operation, @ApiResponse…)
│   ├── request/      # Request DTOs (Java records + @Valid)
│   ├── response/     # Response DTOs (Java records)
│   └── support/      # Utilitários de paginação (PaginationUtils)
├── exception/        # Exceções personalizadas
├── mapper/           # Mappers MapStruct (toEntity / toResponse)
├── entity/           # Entidades JPA + enums (RepairOrderStatus, PaymentStatus, PaymentMethod, Role)
├── repository/       # Repositórios Spring Data + Specifications (filtros dinâmicos)
└── service/          # Regras de negócio (inclui PaymentReceiptService, RefreshTokenService, LoginAttemptService)

src/main/resources/
├── application.properties
└── db/migration/     # Migrations Flyway (V1..V16, append-only)

.github/workflows/
└── ci.yml            # Pipeline de CI (build + testes + cobertura)
```

---

### Pré-requisitos

* Java 21+
* Maven (ou use o wrapper `./mvnw`)
* PostgreSQL 16 (ou Docker)
* Docker em execução para rodar os testes de integração (Testcontainers)

---

### Rodando localmente

1. Copie o arquivo de exemplo de variáveis de ambiente e ajuste os valores:

   ```bash
   cp .env.example .env
   ```

   O `.env` alimenta o `docker-compose.yml` e o `application.properties` (banco, `JWT_SECRET`, conta ADMIN inicial, dados da loja para o recibo `SHOP_*` e, quando a aplicação for hospedada, as credenciais do Mercado Pago `MP_*`). Ele **nunca** é commitado.

2. Suba o banco de dados via Docker Compose (apenas o serviço `db`):

   ```bash
   docker compose up -d
   ```

   > Se não usar Docker, crie o banco manualmente: `CREATE DATABASE eletrolonghi;`

3. Rode a aplicação (o Flyway aplica as migrations pendentes na inicialização):

   ```bash
   ./mvnw spring-boot:run
   ```

4. Acesse:
   * API: `http://localhost:8080`
   * Swagger UI: `http://localhost:8080/swagger-ui/index.html`
   * OpenAPI JSON: `http://localhost:8080/api/api-docs`

> **Atenção:** O `docker-compose.yml` sobe **apenas o banco de dados**. A aplicação deve ser executada localmente via Maven ou IDE.

#### Primeiro acesso

Na primeira inicialização é criada uma conta `ADMIN` a partir de `ADMIN_EMAIL` / `ADMIN_PASSWORD` (definidos no `.env`). Novos usuários criados via `POST /auth/register` nascem **desativados** — um `ADMIN` precisa ativá-los (`PATCH /user/{id}/status` com `enabled: true`) e, se necessário, ajustar o perfil (`PATCH /user/{id}/role`).

---

### Build e testes

```bash
# Build completo (compila + roda todos os testes)
./mvnw package

# Build sem testes
./mvnw -DskipTests package

# Testes + relatório de cobertura + gate de cobertura (JaCoCo)
./mvnw verify
```

> **Nota:** Os testes de integração usam **Testcontainers** e sobem o próprio PostgreSQL — exigem um Docker daemon em execução. O `./mvnw verify` executa toda a suíte, gera o relatório JaCoCo e **falha se a cobertura de linha cair abaixo de 80%** (`coverage.minimum` em `pom.xml`).

---

### Integração Contínua (CI)

O workflow [`.github/workflows/ci.yml`](.github/workflows/ci.yml) roda no GitHub Actions em:

* todo **push** para `main`;
* todo **pull request** para `main`;
* execução manual (`workflow_dispatch`).

Cada execução faz `./mvnw verify` em `ubuntu-latest` com JDK 21 (Temurin) e cache do repositório Maven — ou seja, roda os testes unitários, os testes de integração com Testcontainers e o gate de cobertura de 80%. Os relatórios de Surefire e JaCoCo são publicados como artefato do job para inspeção.

**Proteção da branch `main`:** um *ruleset* exige que o status check `test` passe e que exista um Pull Request antes do merge, de modo que **nenhuma mudança entra em `main` com a suíte quebrada**.

---

### Autenticação e autorização

* **Access token** JWT (Bearer), expira em `spring.security.access-token-expiration-seconds` (padrão 1h).
* **Refresh token** persistido no banco, expira em `spring.security.refresh-token-expiration-ms` (padrão 7 dias). A cada `POST /auth/refresh` o token é **rotacionado** e os anteriores do usuário são revogados — cada usuário tem no máximo um refresh token válido.
* **Brute-force**: após `spring.security.login.max-attempts` falhas o e-mail fica bloqueado por `spring.security.login.block-duration-ms` (controle em memória).
* **Rotas públicas**: `POST /auth/register`, `POST /auth/login`, `POST /auth/refresh`, `POST /auth/logout` e o Swagger. Todo o resto exige `Authorization: Bearer <token>`.
* **Rotas exclusivas de `ADMIN`** (via regras de URL no `SecurityConfig`): `POST`/`DELETE /brand`, `POST`/`DELETE /accessory`, `DELETE /customer/{id}`, `DELETE /device/{id}`, `DELETE /repair-order/{id}`, `GET /user`, `PATCH /user/{id}/role`, `PATCH /user/{id}/status`.

---

### Workflow de status da Ordem de Reparo

O status é um fluxo ordenado (`entity/enums/RepairOrderStatus`), não um enum livre:

```
AWAITING_EVALUATION → IN_EVALUATION → AWAITING_APPROVAL → APPROVED
→ AWAITING_PARTS → IN_REPAIR → REPAIR_COMPLETED → PAYMENT_RECEIVED → DEVICE_COLLECTED
```

A transição é feita por `PATCH /repair-order/{id}/status` (payload `RepairOrderStatusUpdateRequest`) e também é validada no `PUT`: só é permitido avançar ou retroceder **uma etapa por vez** — transições fora de ordem retornam `422`. Um aparelho só pode ter uma nova ordem aberta quando a ordem anterior atingiu `DEVICE_COLLECTED` (caso contrário, `422`).

---

### Endpoints

> Rotas públicas: `POST /auth/register`, `POST /auth/login`, `POST /auth/refresh`, `POST /auth/logout` e o Swagger. As demais exigem `Authorization: Bearer <token>`. `401` = não autenticado, `403` = autenticado mas sem permissão (`ADMIN`).
>
> Parâmetros de paginação (onde há listagem paginada): `page`, `size`, `sortBy`, `direction`.

#### Autenticação (`/auth`)

| Método | Endpoint | Descrição | Status |
|---|---|---|---|
| `POST` | `/auth/register` | Registra novo usuário (nasce desativado) | 201 / 400 / 409 |
| `POST` | `/auth/login` | Gera access + refresh token | 200 / 400 / 401 / 403 / 429 |
| `POST` | `/auth/refresh` | Renova o access token e rotaciona o refresh token | 200 / 400 / 401 |
| `POST` | `/auth/logout` | Revoga o refresh token informado (idempotente) | 204 / 400 |

#### Usuários (`/user`) — **ADMIN**

| Método | Endpoint | Descrição | Status |
|---|---|---|---|
| `GET` | `/user` | Lista usuários (paginado + filtros: `name`, `email`, `role`, `enabled`) | 200 / 401 / 403 |
| `PATCH` | `/user/{id}/role` | Altera o perfil (`ADMIN`/`USER`) | 200 / 400 / 401 / 403 / 404 |
| `PATCH` | `/user/{id}/status` | Ativa ou suspende a conta (`enabled`) | 200 / 400 / 401 / 403 / 404 |

#### Marca (`/brand`)

| Método | Endpoint | Descrição | Status |
|---|---|---|---|
| `GET` | `/brand` | Lista todas as marcas | 200 / 401 |
| `GET` | `/brand/{id}` | Busca marca por ID | 200 / 401 / 404 |
| `POST` | `/brand` | Cria nova marca — **ADMIN** | 201 / 400 / 401 / 403 / 409 |
| `DELETE` | `/brand/{id}` | Remove marca — **ADMIN** | 204 / 401 / 403 / 404 / 409 |

#### Aparelho (`/device`)

| Método | Endpoint | Descrição | Status |
|---|---|---|---|
| `GET` | `/device` | Lista aparelhos (paginado + filtros: `model`, `brandId`) | 200 / 401 |
| `GET` | `/device/{id}` | Busca aparelho por ID | 200 / 401 / 404 |
| `GET` | `/device/serial-number?serialNumber=` | Busca por número de série | 200 / 401 / 404 |
| `POST` | `/device` | Cria aparelho (`serialNumber` único) | 201 / 400 / 401 / 409 |
| `PUT` | `/device/{id}` | Atualiza aparelho | 200 / 400 / 401 / 404 / 409 |
| `DELETE` | `/device/{id}` | Remove aparelho — **ADMIN** | 204 / 401 / 403 / 404 / 409 |

#### Cliente (`/customer`)

| Método | Endpoint | Descrição | Status |
|---|---|---|---|
| `GET` | `/customer` | Lista clientes (paginado + filtros: `name`, `email`, `phone`) | 200 / 401 |
| `GET` | `/customer/{id}` | Busca cliente por ID | 200 / 401 / 404 |
| `POST` | `/customer` | Cria cliente (`email` único) | 201 / 400 / 401 / 409 |
| `PUT` | `/customer/{id}` | Atualiza cliente | 200 / 400 / 401 / 404 / 409 |
| `DELETE` | `/customer/{id}` | Remove cliente — **ADMIN** | 204 / 401 / 403 / 404 / 409 |

#### Acessório (`/accessory`)

| Método | Endpoint | Descrição | Status |
|---|---|---|---|
| `GET` | `/accessory` | Lista todos os acessórios | 200 / 401 |
| `GET` | `/accessory/{id}` | Busca acessório por ID | 200 / 401 / 404 |
| `POST` | `/accessory` | Cria acessório — **ADMIN** | 201 / 400 / 401 / 403 / 409 |
| `DELETE` | `/accessory/{id}` | Remove acessório — **ADMIN** | 204 / 401 / 403 / 404 / 409 |

#### Ordem de Reparo (`/repair-order`)

| Método | Endpoint | Descrição | Status |
|---|---|---|---|
| `GET` | `/repair-order` | Lista reparos (paginado + filtros: `status`, `customerId`, `deviceId`, `createdFrom`, `createdTo`) | 200 / 401 |
| `GET` | `/repair-order/{id}` | Busca reparo por ID | 200 / 401 / 404 |
| `POST` | `/repair-order` | Cria reparo | 201 / 400 / 401 / 404 / 422 |
| `PUT` | `/repair-order/{id}` | Atualiza reparo | 200 / 400 / 401 / 404 / 422 |
| `PATCH` | `/repair-order/{id}/status` | Transição de status (uma etapa por vez) | 200 / 400 / 401 / 404 / 422 |
| `DELETE` | `/repair-order/{id}` | Remove reparo — **ADMIN** | 204 / 401 / 403 / 404 |

#### Pagamento (`/payment`)

| Método | Endpoint | Descrição | Status |
|---|---|---|---|
| `GET` | `/payment` | Lista pagamentos (paginado + filtros: `status`, `method`, `repairOrderId`, `createdFrom`, `createdTo`) | 200 / 401 |
| `GET` | `/payment/{id}` | Busca pagamento por ID | 200 / 401 / 404 |
| `POST` | `/payment` | Registra pagamento de uma ordem (um por ordem) | 201 / 400 / 401 / 404 / 422 |
| `PUT` | `/payment/{id}` | Atualiza dados do pagamento | 200 / 400 / 401 / 404 |
| `PATCH` | `/payment/{id}/status` | Altera a situação; `APPROVED` avança a ordem para `PAYMENT_RECEIVED` | 200 / 400 / 401 / 404 |
| `GET` | `/payment/{id}/receipt` | Recibo do pagamento em PDF (não-fiscal) | 200 / 401 / 404 |
| `DELETE` | `/payment/{id}` | Remove pagamento — **ADMIN** | 204 / 401 / 403 / 404 |

---

### Decisões de Design

* **DTOs como records**: imutabilidade e concisão; campos acessados via métodos sem prefixo (ex: `request.brand()`).
* **Contratos OpenAPI separados**: controllers implementam interfaces `*Api` de `controller/api/spec/`, facilitando evolução e documentação.
* **MapStruct para mapeamento**: geração em tempo de compilação; convenções `toEntity(Request)` e `toResponse(Entity)`.
* **Validações e erros de domínio**: `@Valid` nos DTOs; tradução global via `@RestControllerAdvice` — `MethodArgumentNotValidException` → 400, `DataIntegrityViolationException` → 409, regras de negócio (aparelho já em reparo, transição de status inválida) → 422, entidade referenciada inexistente → 404.
* **Segurança stateless**: Spring Security + JWT (Bearer), sem sessão no servidor; 401 explícito via `AuthenticationEntryPoint`.
* **Autorização por URL, sem method security**: os papéis são aplicados em `SecurityConfig` com `requestMatchers(...).hasRole("ADMIN")` — não há `@PreAuthorize` no projeto.
* **Refresh token com rotação e revogação**: cada usuário mantém no máximo um refresh token válido; suspender a conta interrompe a emissão de novos access tokens imediatamente.
* **Ativação de conta por ADMIN**: contas auto-registradas começam bloqueadas, evitando acesso não supervisionado.
* **Paginação avançada**: `Device`, `Customer`, `RepairOrder`, `Payment` e `User` usam `JpaSpecificationExecutor` para filtros dinâmicos; `Brand` e `Accessory` (tabelas de lookup) retornam lista simples.
* **Pagamento desacoplado do reparo**: grupo de rotas `/payment` próprio (1 pagamento por ordem), recibo em PDF via OpenPDF e cliente do Mercado Pago como esqueleto — a maquininha física usa a API Point e será ligada quando a aplicação estiver hospedada (confirmação por webhook ou polling).
* **Status de reparo como workflow**: enum ordenado com endpoint `PATCH` dedicado, em vez de aceitar qualquer valor no `PUT`.
* **ENUM no banco como VARCHAR**: simplicidade sem dependências externas.
* **Migrations append-only**: nunca editar arquivos `V*.sql` existentes; alterações sempre em novos scripts.
* **Testes de integração com Testcontainers (container singleton)**: um único PostgreSQL reaproveitado entre as classes de teste; propriedades de datasource injetadas via `@DynamicPropertySource`.

---

### Melhorias Futuras

* Implementar enums nativos no banco (PostgreSQL) com driver customizado.
* Métricas de performance e observabilidade (Actuator / Micrometer).
* Containerizar a aplicação (imagem própria + serviço no `docker-compose.yml`).
* Substituir o controle de brute-force em memória por um armazenamento compartilhado (Redis) para suportar múltiplas instâncias.
* Publicar o relatório de cobertura (badge/artefato) e adicionar checagem de vulnerabilidades de dependências no CI.

---

*Desenvolvido por [Carlos Longhi](https://carloslonghi.com.br).*
