
# Eletro Longhi API

### Descrição do Projeto

A **Eletro Longhi API** é um backend em Java (Spring Boot) para gerenciar todo o fluxo de conserto de equipamentos em uma eletrônica.

Funcionalidades implementadas:

* **Autenticação** via JWT (Bearer token) com endpoints de registro e login.
* CRUD de **Marcas** (`/brand`).
* CRUD de **Clientes** (`/customer`).
* CRUD de **Aparelhos** (`/device`) e busca por marca (`/device/search?brandId=`).
* CRUD de **Acessórios** (`/accessory`).
* CRUD de **Ordens de Reparo** (`/repair-order`), incluindo status e vinculação 1:1 com aparelho.
* **Controle de integridade**: tratamento de conflitos (409) e validações (`@Valid`).
* Documentação **OpenAPI/Swagger** disponível em `/swagger-ui/index.html`.

---

### Tecnologias Utilizadas

* **Java 21**  
* **Spring Boot 3** (Web, Security, Data JPA, Validation)  
* **Flyway** (migrations)  
* **PostgreSQL**  
* **Docker & Docker Compose**  
* **Springdoc OpenAPI** (Swagger UI)  
* **Lombok**  

---

### Organização do Código

```text
src/main/java/br/com/carloslonghi/eletrolonghi/
├── config/           # Configurações gerais (Security, JWT, Swagger) e ControllerAdvice
├── controller/       # Controllers REST (implementam *Api interfaces*)
│   └── api/spec/     # Interfaces de documentação Swagger (@Operation, @ApiResponse…)
│   └── request/      # Request DTOs anotados com @Schema
│   └── response/     # Response DTOs anotados com @Schema
├── exception/        # Exceções personalizadas
├── mapper/           # Mappers entre DTOs e Entities
├── entity/           # Entidades JPA + enums
├── repository/       # Repositórios Spring Data
└── service/          # Regras de negócio

src/main/java/resources/
└── db/migration      # Migrations do banco de dados
````

---

### Docker Compose

O repositório inclui um `docker-compose.yml` que orquestra:

* **app**: aplicação Spring Boot
* **db**: container PostgreSQL

### Executando com Docker

1. Clone o repositório:

   ```bash
   git clone https://github.com/CarlosLonghi/API-Eletro-Longhi
   cd API-Eletro-Longhi
   ```
2. Ajuste as credenciais em `src/main/resources/application.properties` se necessário.
3. Inicie tudo:

   ```bash
   docker compose up -d --build
   ```
4. Acesse a API em `http://localhost:8080` e o Swagger UI em `http://localhost:8080/swagger-ui/index.html`.

---

### Rodando localmente

1. Crie o banco vazio:

   ```sql
   CREATE DATABASE eletro_longhi;
   ```
2. Configure `application.properties`:

   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/eletro_longhi
   spring.datasource.username=postgres
   spring.datasource.password=postgres

   spring.flyway.baseline-on-migrate=true
   springdoc.override-with-generic-response=false
   ```
3. Rode via Maven ou IDE:

   ```bash
   ./mvnw spring-boot:run
   ```

---

### Endpoints Principais

#### Autenticação

* `POST /auth/register` – Registra novo usuário (201 / 400 / 409)
* `POST /auth/login`    – Gera JWT (200 / 400 / 401)

#### Marca

* `GET  /brand`         – Lista marcas (200 / 403)
* `POST /brand`         – Cria marca (201 / 400 / 409)
* `GET  /brand/{id}`    – Busca por ID (200 / 403 / 404)
* `DELETE /brand/{id}`  – Remove marca (204 / 403 / 404 / 409)

#### Cliente

* `GET  /customer`       – Lista clientes (200 / 403)
* `POST /customer`       – Cria cliente (201 / 400)
* `GET  /customer/{id}`  – Busca por ID (200 / 403 / 404)
* `PUT  /customer/{id}`  – Atualiza cliente (200 / 400 / 403)
* `DELETE /customer/{id}`– Remove cliente (204 / 403 / 404 / 409)

### Aparelho

* `GET  /device`         – Lista aparelhos (200 / 403)
* `POST /device`         – Cria aparelho (201 / 400)
* `GET  /device/{id}`    – Busca por ID (200 / 403 / 404)
* `PUT  /device/{id}`    – Atualiza aparelho (200 / 400 / 403)
* `DELETE /device/{id}`  – Remove aparelho (204 / 403 / 404 / 409)
* `GET  /device/search?brandId={id}` – Filtra aparelhos por marca (200 / 403)

#### Acessório

* `GET  /accessory`        – Lista acessórios (200 / 403)
* `POST /accessory`        – Cria acessório (201 / 400 / 409)
* `GET  /accessory/{id}`   – Busca por ID (200 / 403 / 404)
* `DELETE /accessory/{id}` – Remove acessório (204 / 403 / 404 / 409)

#### Reparo

* `GET  /repair-order`       – Lista todos os reparos (200 / 403)
* `POST /repair-order`       – Cria reparo (201 / 400 / 403 / 409)
* `GET  /repair-order/{id}`  – Busca por ID (200 / 403 / 404)
* `PUT  /repair-order/{id}`  – Atualiza reparo (200 / 400 / 403 / 409)
* `DELETE /repair-order/{id}`– Remove reparo (204 / 403 / 404)

---

### Decisões de Design

* **DTO vs Entity**: separação de camadas com mappers bem definidos.
* **Validações**: `@Valid`, `@NotNull`, `@NotBlank` e tratamento global via `@RestControllerAdvice`.
* **Erros Genéricos**: `ErrorResponse` padronizado e tradução de constraints SQL.
* **ENUM no Banco**: optei por usar `VARCHAR` para simplicitade sem dependências externas.
* **Segurança**: Spring Security + JWT (Bearer).

---

### Melhorias futuras

* Implementar enums nativos no banco (PostgreSQL) com driver custom.
* Adicionar testes unitários e de integração.
* Paginação e filtros avançados nas listagens.
* CI/CD e métricas de performance.

---

*Desenvolvido por [Carlos Longhi](https://carloslonghi.com.br).*
