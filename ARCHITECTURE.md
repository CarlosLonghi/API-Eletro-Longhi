# Arquitetura — Resumo Rápido

Este arquivo contém um resumo leve da arquitetura do projeto para referência rápida.

---

## Objetivo

- Fornecer um mapa conciso das camadas do sistema (controller → service → repository).
- Indicar pontos de integração relevantes para agentes/IA (endpoints de OpenAPI, pontos de extensão para mappers e serviços).
- Servir como referência durante desenvolvimento e integração com modelos de IA.

---

## Visão Geral

- **Linguagem e framework:** Java 21 + Spring Boot 3
- **BD:** PostgreSQL 16 com migrations Flyway
- **Autenticação:** JWT Bearer token via `config/TokenService`
- **Organização:** Por recursos em `br.com.carloslonghi.eletrolonghi`

---

## Estrutura de Pacotes

```
br.com.carloslonghi.eletrolonghi/
├── config/          # Segurança, Token, Swagger, Error Handling
├── controller/      # Endpoints REST
│   ├── request/     # DTOs de entrada (records)
│   └── response/    # DTOs de saída (records)
├── service/         # Lógica de negócio
├── repository/      # Spring Data JPA
├── mapper/          # MapStruct (DTO ↔ Entity)
├── entity/          # Entidades JPA
└── exception/       # Exceções personalizadas
```

---

## Fluxo de Requisição

```
HTTP Request
    ↓
Controller (recebe DTO record)
    ↓
Mapper.toEntity() — converte DTO → Entity
    ↓
Service — executa lógica, valida
    ↓
Repository — persiste em PostgreSQL
    ↓
Mapper.toResponse() — converte Entity → DTO
    ↓
HTTP Response (DTO record)
```

---

## Pontos Importantes

### 1. Security (JWT)

- **Geração:** `config/TokenService.generateToken(userId, userName)`
- **Verificação:** `config/TokenService.verifyToken(token)`
- **Aplicação:** `config/SecurityFilter` extrai token do header `Authorization: Bearer <token>`
- **Config:** `config/SecurityConfig` define rotas públicas (`/auth/*`)

### 2. DTOs (Imutáveis)

- Records em `controller/request/` e `controller/response/`
- Anotadas com `@Valid`, `@NotNull`, `@NotBlank` para validação
- Validações são traduzidas globalmente por `config/ApplicationControllerAdvice`

### 3. Mappers (MapStruct)

- Interfaces em `mapper/` com `@Mapper(componentModel = "spring")`
- Métodos: `toEntity(RequestRecord)` → Entity, `toResponse(Entity)` → ResponseRecord
- **Injetar sempre** em controllers/services (são Spring beans)
- Implementações geradas em tempo de compilação

### 4. Services & Repositories

- Services retornam `Optional` para single entity, `List` para coleções
- Repositories usam Spring Data naming (ex: `findDevicesByBrandId`)
- Controllers usam `Optional.map()` para decidir 200 vs 404

### 5. Migrations BD

- Flyway em `src/main/resources/db/migration/`
- Arquivos numerados sequencialmente (V1__, V2__, ... V8__, etc.)
- PostgreSQL SQL, sem DROP (use ALTER para compatibilidade)

### 6. Error Handling

- `ApplicationControllerAdvice` traduz exceções globalmente:
  - `MethodArgumentNotValidException` → 400 (com field errors)
  - `DataIntegrityViolationException` → 409 (conflito)
  - `ResourceNotFoundException` (custom) → 404
- Responses sempre usam estrutura padronizada

---

## Endpoints Exemplo

### Autenticação

```
POST /auth/register   — Registra novo usuário
POST /auth/login      — Gera JWT
```

### CRUD Padrão

```
GET    /api/brand           — Lista (requer JWT)
POST   /api/brand           — Cria (requer JWT)
GET    /api/brand/{id}      — Detalhes (requer JWT)
PUT    /api/brand/{id}      — Atualiza (requer JWT)
DELETE /api/brand/{id}      — Remove (requer JWT)
```

---

## Convenções Críticas

| Aspecto | Padrão | Exemplo |
|--------|--------|---------|
| **DTO** | Record imutável | `public record DeviceRequest(@NotNull Long brandId, ...)` |
| **Entity** | JPA com Lombok | `@Entity @Data public class DeviceEntity { ... }` |
| **Mapper** | MapStruct interface | `@Mapper(componentModel = "spring") public interface DeviceMapper { ... }` |
| **Repository** | Spring Data | `public interface DeviceRepository extends JpaRepository { ... }` |
| **Service** | Return Optional/List | `public Optional<Device> findById(Long id) { ... }` |
| **Controller** | Endpoint + mapper + service | `@PostMapping @Valid @RequestBody` + mapper.toEntity() |
| **Validação** | Anotações + Advice | `@NotNull`, `@NotBlank` → ApplicationControllerAdvice |
| **Segurança** | JWT Bearer | `Authorization: Bearer <token>` |

---

## Como Adicionar Nova Feature

1. **Criar records** em `controller/request/` e `controller/response/`
2. **Criar mapper** em `mapper/` (MapStruct interface)
3. **Criar service** em `service/` (lógica, usa repository)
4. **Criar/atualizar repository** em `repository/` (queries)
5. **Criar controller** em `controller/` (endpoints, usa service + mapper)
6. **Criar migration** em `src/main/resources/db/migration/` (se BD muda)
7. **Compilar** via `./mvnw compile` (MapStruct gera implementations)

---

## Referências Importantes

| Arquivo | Propósito |
|---------|-----------|
| [`AGENTS.md`](AGENTS.md) | Checklist completo para agentes de IA |
| [`docs/QUICK_START_FOR_AGENTS.md`](docs/QUICK_START_FOR_AGENTS.md) | Resumo 15 minutos |
| [`docs/STRUCTURE.md`](docs/STRUCTURE.md) | Mapa detalhado de diretórios |
| [`docs/AGENT_PROMPTS.md`](docs/AGENT_PROMPTS.md) | Templates de prompts para LLMs |
| [`docs/TROUBLESHOOTING.md`](docs/TROUBLESHOOTING.md) | Resolução de problemas |

---

## Checklist Rápido

Antes de commitar código:

- [ ] Novo DTOs: records com `@Valid`, `@NotNull`, `@NotBlank`
- [ ] Novo mapper: `@Mapper(componentModel = "spring")` com `toEntity()` e `toResponse()`
- [ ] Novo service: retorna `Optional` (single) ou `List` (coleção)
- [ ] Novo endpoint: usa mapper.toEntity(), service, mapper.toResponse()
- [ ] Nova migração: arquivo V[N]__ em db/migration/
- [ ] Compilou sem erro: `./mvnw compile`
- [ ] Validações funcionam: use `@Valid` + anotações
- [ ] Testou via Swagger: `/swagger-ui/index.html`

---

**Desenvolva com confiança! Use este documento como referência rápida. Para detalhes, consulte `docs/` ou `AGENTS.md`.**


