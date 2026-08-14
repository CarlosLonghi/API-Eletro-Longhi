# Eletro Longhi API

### Descrição do Projeto

A **Eletro Longhi API** é um backend em Java (Spring Boot) para gerenciar todo o fluxo de conserto de equipamentos em uma eletrônica.

Funcionalidades implementadas:

* **Autenticação** via JWT (Bearer token) com endpoints de registro e login.
* CRUD de **Marcas** (`/brand`).
* CRUD de **Clientes** (`/customer`) com listagem paginada e filtros.
* CRUD de **Aparelhos** (`/device`) com listagem paginada, busca por marca e por número de série.
* CRUD de **Acessórios** (`/accessory`).
* CRUD de **Ordens de Reparo** (`/repair-order`) com listagem paginada e filtros por status, cliente e aparelho.
* **Controle de integridade**: tratamento de conflitos (409) e validações (`@Valid`).
* Documentação **OpenAPI/Swagger** disponível em `/swagger-ui/index.html`.

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
| Lombok | (gerenciado pelo Spring Boot) |
| Testcontainers (PostgreSQL) | 1.21.4 |
| JaCoCo (Code Coverage) | 0.8.12 |
| Docker & Docker Compose | — |

---

### Organização do Código

```text
src/main/java/br/com/carloslonghi/eletrolonghi/
├── config/           # Configurações gerais (Security, JWT, Swagger) e ControllerAdvice
├── controller/       # Controllers REST (implementam interfaces *Api)
│   ├── api/spec/     # Interfaces de contrato OpenAPI (@Operation, @ApiResponse…)
│   ├── request/      # Request DTOs (Java records + @Valid)
│   ├── response/     # Response DTOs (Java records)
│   └── support/      # Utilitários de paginação (PaginationUtils)
├── exception/        # Exceções personalizadas
├── mapper/           # Mappers MapStruct (toEntity / toResponse)
├── entity/           # Entidades JPA + enums
├── repository/       # Repositórios Spring Data + Specifications
└── service/          # Regras de negócio

src/main/resources/
├── application.properties
└── db/migration/     # Migrations Flyway (V1..V13)
```

---

### Pré-requisitos

* Java 21+
* Maven (ou use o wrapper `./mvnw`)
* PostgreSQL 16 (ou Docker)

---

### Rodando localmente

1. Suba o banco de dados via Docker Compose (apenas o serviço `db`):

   ```bash
   docker compose up -d
   ```

2. Crie o banco caso não exista:

   ```sql
   CREATE DATABASE eletrolonghi;
   ```

3. Configure `src/main/resources/application.properties`:

   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/eletrolonghi
   spring.datasource.username=postgres
   spring.datasource.password=postgres

   spring.security.secret=sua-chave-secreta-jwt
   ```

4. Rode a aplicação:

   ```bash
   ./mvnw spring-boot:run
   ```

5. Acesse:
   * API: `http://localhost:8080`
   * Swagger UI: `http://localhost:8080/swagger-ui/index.html`
   * OpenAPI JSON: `http://localhost:8080/api/api-docs`

> **Atenção:** O `docker-compose.yml` sobe **apenas o banco de dados**. A aplicação deve ser executada localmente via Maven ou IDE.

---

### Build

```bash
# Build completo (com testes unitários e integração)
./mvnw package

# Build sem testes
./mvnw -DskipTests package

# Build com relatório de cobertura de testes (JaCoCo)
./mvnw verify
```

> **Nota:** Testes de integração requerem Docker para usar Testcontainers (PostgreSQL). O comando `./mvnw verify` executa todos os testes e gera relatório de cobertura; falha se a cobertura cair abaixo de 80% (configurável em `pom.xml`).

---

### Endpoints Principais

> Todos os endpoints (exceto `/auth/register` e `/auth/login`) requerem autenticação via `Authorization: Bearer <token>`.

#### Autenticação

| Método | Endpoint | Descrição | Status |
|---|---|---|---|
| `POST` | `/auth/register` | Registra novo usuário | 201 / 400 / 409 |
| `POST` | `/auth/login` | Gera token JWT | 200 / 400 / 401 |

#### Marca

| Método | Endpoint | Descrição | Status |
|---|---|---|---|
| `GET` | `/brand` | Lista todas as marcas | 200 / 403 |
| `POST` | `/brand` | Cria nova marca | 201 / 400 / 409 |
| `GET` | `/brand/{id}` | Busca marca por ID | 200 / 403 / 404 |
| `DELETE` | `/brand/{id}` | Remove marca | 204 / 403 / 404 / 409 |

#### Aparelho

Parâmetros de paginação: `page`, `size`, `sortBy`, `direction`

| Método | Endpoint | Descrição | Status |
|---|---|---|---|
| `GET` | `/device` | Lista aparelhos (paginado) | 200 / 403 |
| `POST` | `/device` | Cria aparelho | 201 / 400 |
| `GET` | `/device/{id}` | Busca aparelho por ID | 200 / 403 / 404 |
| `PUT` | `/device/{id}` | Atualiza aparelho | 200 / 400 / 403 |
| `DELETE` | `/device/{id}` | Remove aparelho | 204 / 403 / 404 / 409 |
| `GET` | `/device/search?model=&brandId=` | Filtra aparelhos (paginado) | 200 / 403 |
| `GET` | `/device/serial-number?serialNumber=` | Busca por número de série | 200 / 403 / 404 |

#### Cliente

Parâmetros de paginação: `page`, `size`, `sortBy`, `direction`

| Método | Endpoint | Descrição | Status |
|---|---|---|---|
| `GET` | `/customer` | Lista clientes (paginado + filtros: `name`, `email`, `phone`) | 200 / 403 |
| `POST` | `/customer` | Cria cliente | 201 / 400 |
| `GET` | `/customer/{id}` | Busca cliente por ID | 200 / 403 / 404 |
| `PUT` | `/customer/{id}` | Atualiza cliente | 200 / 400 / 403 |
| `DELETE` | `/customer/{id}` | Remove cliente | 204 / 403 / 404 / 409 |

#### Acessório

| Método | Endpoint | Descrição | Status |
|---|---|---|---|
| `GET` | `/accessory` | Lista todos os acessórios | 200 / 403 |
| `POST` | `/accessory` | Cria acessório | 201 / 400 / 409 |
| `GET` | `/accessory/{id}` | Busca acessório por ID | 200 / 403 / 404 |
| `DELETE` | `/accessory/{id}` | Remove acessório | 204 / 403 / 404 / 409 |

#### Ordem de Reparo

Parâmetros de paginação: `page`, `size`, `sortBy`, `direction`

| Método | Endpoint | Descrição | Status |
|---|---|---|---|
| `GET` | `/repair-order` | Lista reparos (paginado + filtros: `status`, `customerId`, `deviceId`, `createdFrom`, `createdTo`) | 200 / 403 |
| `POST` | `/repair-order` | Cria reparo | 201 / 400 / 403 / 409 |
| `GET` | `/repair-order/{id}` | Busca reparo por ID | 200 / 403 / 404 |
| `PUT` | `/repair-order/{id}` | Atualiza reparo | 200 / 400 / 403 / 409 |
| `DELETE` | `/repair-order/{id}` | Remove reparo | 204 / 403 / 404 |

---

### Decisões de Design

* **DTOs como records**: imutabilidade e concisão; campos acessados via métodos sem prefixo (ex: `request.brand()`).
* **Contratos OpenAPI separados**: controllers implementam interfaces `*Api` de `controller/api/spec/`, facilitando evolução e documentação.
* **MapStruct para mapeamento**: geração em tempo de compilação; convenções `toEntity(Request)` e `toResponse(Entity)`.
* **Validações**: `@Valid`, `@NotNull`, `@NotBlank` nos DTOs; tradução global via `@RestControllerAdvice`.
* **Segurança stateless**: Spring Security + JWT (Bearer) sem sessão no servidor.
* **Paginação avançada**: `Device`, `Customer` e `RepairOrder` usam `JpaSpecificationExecutor` para filtros dinâmicos.
* **ENUM no banco como VARCHAR**: simplicidade sem dependências externas.
* **Migrations append-only**: nunca editar arquivos `V*.sql` existentes; alterações sempre em novos scripts.

---

### Melhorias Futuras

* Implementar enums nativos no banco (PostgreSQL) com driver customizado.
* Pipeline CI/CD e métricas de performance.
* Containerizar a aplicação no `docker-compose.yml`.
* Expandir cobertura de testes unitários (atualmente em 80%, com suporte a integração via Testcontainers).

---

*Desenvolvido por [Carlos Longhi](https://carloslonghi.com.br).*
