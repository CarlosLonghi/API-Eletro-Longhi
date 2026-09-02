# Eletro Longhi API

Java 21 + Spring Boot 4.1 REST API for a device-repair shop: customers bring in devices, staff track repair orders through a status workflow. Package root: `br.com.carloslonghi.eletrolonghi`.

## Build / run / test

- Run locally: `./mvnw spring-boot:run` (reads `src/main/resources/application.properties`)
- Compile only: `./mvnw -q -DskipTests package` — always recompile after touching a `mapper/*.java` interface, since MapStruct generates the implementation at compile time.
- Tests: `./mvnw test` (unit + Testcontainers-backed repository integration tests — needs a working Docker daemon).
- `./mvnw verify` additionally runs JaCoCo `check` and **fails the build below `coverage.minimum` (0.80)** — run this before considering a change done.
- `docker compose up -d` starts only Postgres (`db` service, postgres:16); the app itself is not containerized, run it with the Maven command above.
- DB must exist beforehand: `CREATE DATABASE eletrolonghi;` (or let docker compose provide it). Flyway applies pending migrations from `src/main/resources/db/migration` on startup.
- Swagger UI: `/swagger-ui/index.html`. OpenAPI docs path: `/api/api-docs` (see `springdoc.*` in `application.properties`).

## Commit convention

- **Semantic commits** (Conventional Commits): `feat:`, `fix:`, `chore:`, `docs:`, `test:`, `refactor:`, `build:`, `ci:`. Keep the subject line imperative and concise.
- **Split the work into smaller, cohesive commits** — never one giant commit with the whole feature. Each commit should stand on its own and group related changes (e.g. persistence model, service layer, endpoints, docs). Don't over-granularize: group what belongs together, don't commit file by file.
- **Do not add AI credits** to commits: no `Co-Authored-By: Claude`, `Generated with Claude Code`, `Claude-Session:` or equivalent trailers.
- The commit body (when useful) explains the *why* of the change, not a restatement of the diff.

## Layered architecture

`controller` (record DTOs, `@Valid`) → `service` (business rules) → `repository` (Spring Data JPA) → `entity` (JPA POJOs), with `mapper/*` (MapStruct, `@Mapper(componentModel = "spring")`) converting DTO ↔ entity.

- Controllers implement OpenAPI contract interfaces in `controller/api/spec/*Api.java` — update the interface signature/docs together with the controller method when changing an endpoint contract.
- Controllers never return entities directly, always `mapper.toResponse(entity)`.
- Mappers expose `toEntity(RequestRecord)` and `toResponse(Entity)`; some carry default helper methods for id → placeholder-entity resolution (e.g. `brandFromId`, `accessoryFromId`) and `List<Long>` → `List<Entity>` conversions. Don't hand-write static mapper utility classes — MapStruct generates the implementation.
- Services return `Optional<Entity>` for single lookups; controllers translate to 200/404.
- Listing strategy is deliberately split:
  - `Brand`, `Accessory` — small lookup tables, plain `List`, no pagination (`GET /brand`, `GET /accessory`).
  - `Device`, `Customer`, `RepairOrder`, `Payment` — operational listings, `Pageable` + `JpaSpecificationExecutor`-backed filters (`repository/specification/*`), return `Page`. Shared params: `page`, `size`, `sortBy`, `direction`. Keep this split unless product requirements explicitly change it.
- Repositories use Spring Data derived-query naming (e.g. `DeviceRepository.findDevicesByBrandId`); reach for `Specification` before a custom `@Query`.
- Global exception translation lives in `config/ApplicationControllerAdvice`: `MethodArgumentNotValidException` → 400 + field-errors map, `DataIntegrityViolationException` → 409. Add new validation via `@Valid`/Bean Validation annotations on the request record, not ad hoc checks in services.

## Domain model

- **Brand** — device manufacturer, unique name. One brand → many devices.
- **Device** — unique `serialNumber` (`@NotBlank` + DB unique constraint), FK to `Brand`, many-to-many `Accessory`.
- **Accessory** — lookup table (name, price), many-to-many with `Device`.
- **Customer** — top-level entity (`name`, `phone`, unique `email`), own repository/controller — not embedded in `RepairOrder`.
- **RepairOrder** — `@ManyToOne` to both `Customer` and `Device` (as of `V13`, a device can have more than one repair order over its lifetime; a new order for the same device is only allowed once the previous one reached `DEVICE_COLLECTED` — enforced in `RepairOrderService`, not the DB). Status is `entity/enums/RepairOrderStatus`, a workflow, not a free enum: `AWAITING_EVALUATION → IN_EVALUATION → AWAITING_APPROVAL → APPROVED → AWAITING_PARTS → IN_REPAIR → REPAIR_COMPLETED → PAYMENT_RECEIVED → DEVICE_COLLECTED`. There's a dedicated `PATCH`-style status endpoint using `RepairOrderStatusUpdateRequest`.
- **Payment** — `@ManyToOne` to `RepairOrder`, but **1:1** in practice: `repair_order_id` is `NOT NULL UNIQUE` and `PaymentService.save` rejects a second payment for the same order (`PaymentAlreadyExistsForRepairOrderException` → 422). Fields: `amount` (`BigDecimal`/`NUMERIC(12,2)`), `method` (`entity/enums/PaymentMethod`: `CASH`, `CARD`, `PIX`, `BOLETO`), `status` (`entity/enums/PaymentStatus`: `PENDING`, `APPROVED`, `REJECTED`, `REFUNDED`, `CANCELLED` — **not** a strict workflow), `installments` (>1 only for `CARD`; the service normalizes it to 1 otherwise), `payerName`/`payerDocument` (for the receipt), `externalReference`/`gatewayPaymentId` (nullable, reserved for the future Mercado Pago integration), `paidAt`. Dedicated `PATCH /payment/{id}/status` endpoint; setting status to `APPROVED` stamps `paidAt` and calls `RepairOrderService.markPaymentReceived`, which advances the linked order `REPAIR_COMPLETED → PAYMENT_RECEIVED` (no-op + log in any other state). `GET /payment/{id}/receipt` returns a non-fiscal PDF receipt (`service/PaymentReceiptService`, OpenPDF) with store data from `config/ShopProperties` (`shop.*`). `client/MercadoPagoClient` is a **skeleton** for later (physical card machine = Mercado Pago **Point** API, confirmation by webhook or polling — the app is not hosted yet); only `getPayment` (polling) is implemented.
- **User** — `entity/User implements UserDetails`; `name`, unique `email`, `password` (hashed), `role` (`entity/enums/Role`: `ADMIN`, `USER`, defaults to `USER`) mapped to a `ROLE_<name>` `GrantedAuthority`, `enabled` (defaults `true` at the DB/entity level, but `UserService.save` forces it to `false` for every self-registration — new accounts start locked out until an ADMIN activates them). An ADMIN manages users via `GET /user` (paginated + filterable by `name`/`email`/`role`/`enabled`), `PATCH /user/{id}/role` (role-only), and `PATCH /user/{id}/status` (enabled-only — used both to activate a new account and to suspend/reactivate an existing one). ADMIN-only endpoints: create/delete `Brand`, create/delete `Accessory`, delete `Customer`, delete `Device`, delete `RepairOrder`, delete `Payment`, `GET /user`, `PATCH /user/{id}/role`, `PATCH /user/{id}/status` — enforced via URL-based rules in `SecurityConfig`, not `@PreAuthorize` (this codebase has no method security).
- Migrations are append-only under `src/main/resources/db/migration` (currently V1–V16) — never edit an existing `V*.sql`, always add the next `V{n}`. Confirm the actual current max version with `ls` before citing a number; don't trust stale docs or memory.

## Auth (JWT + refresh tokens)

- `config/TokenService` generates/verifies access tokens; `config/SecurityFilter` extracts claims into `SecurityContext`; `config/SecurityConfig` is stateless and marks `/auth/register`, `/auth/login`, `/auth/refresh`, `/auth/logout` public — everything else needs `Authorization: Bearer <token>`. Invalid/missing tokens get an explicit 401 via a custom `AuthenticationEntryPoint`.
- Authorization beyond "is authenticated" is role-gated in `SecurityConfig` via `requestMatchers(HttpMethod, path).hasRole("ADMIN")` — ADMIN-only: `POST`/`DELETE /brand`, `POST`/`DELETE /accessory`, `DELETE /customer/{id}`, `DELETE /device/{id}`, `DELETE /repair-order/{id}`, `DELETE /payment/{id}`, `GET /user`, `PATCH /user/{id}/role`, `PATCH /user/{id}/status`. Everything else (GET/PUT/PATCH, plus `POST /customer`, `POST /device`, `POST /repair-order`, `POST /payment`) stays open to any authenticated USER or ADMIN.
- A newly self-registered user (`POST /auth/register`) starts `enabled=false` and cannot log in until an ADMIN activates the account (`PATCH /user/{id}/status`, `enabled:true`) and sets its role (`PATCH /user/{id}/role`). Login for a disabled account throws Spring Security's `DisabledException`, caught in `AuthController.login` and translated to `exception/AccountNotActivatedException` → 403. Suspending an already-active user is the same `PATCH /user/{id}/status` endpoint with `enabled:false`; the effect is not instant — `SecurityFilter` keeps trusting JWT claims only (no per-request DB lookup), so an already-issued access token stays valid until it expires naturally, but `RefreshTokenService.findValidToken` checks `user.isEnabled()` so the refresh token stops minting new access tokens right away, and a fresh login attempt is rejected immediately.
- `POST /auth/login` — checks `service/LoginAttemptService` first (in-memory `ConcurrentHashMap`, not shared across instances/restarts — replace with Redis or similar before relying on it in a multi-instance deploy); returns `LoginResponse(token, refreshToken)`.
- `POST /auth/refresh` — validates via `RefreshTokenService.findValidToken` (401 on missing/revoked/expired), issues a new access token **and rotates** the refresh token; `RefreshTokenService.createRefreshToken` revokes all previous tokens for that user first, so a user only ever has one valid refresh token.
- `POST /auth/logout` — revokes the given refresh token, idempotent, 204.
- Relevant properties (`application.properties`): `spring.security.secret`, `spring.security.access-token-expiration-seconds`, `spring.security.refresh-token-expiration-ms`, `spring.security.login.max-attempts`, `spring.security.login.block-duration-ms`, `spring.security.cors.allowed-origins`.
- When changing JWT claims, keep `TokenService.generateToken` and `TokenService.verifyToken` in sync, and check `SecurityFilter` still rebuilds `GrantedAuthority` from the `role` claim correctly.

## Testing conventions

- Unit tests mirror the main package layout under `src/test/java/.../{config,controller,service}`.
- Repository tests are real Postgres integration tests via Testcontainers, extending `repository/support/AbstractPostgresIntegrationTest` — they need Docker and will fail before assertions run if it's unavailable.
- Shared fixtures live in `support/TestFixtures.java`.
- JaCoCo enforces `coverage.minimum` (0.80) on `./mvnw verify`; check coverage before calling a change complete, not just `./mvnw test`.

## Deeper-dive references

For domain detail and structural invariants beyond this file's summary, see `.claude/GLOSSARY.md` and `.claude/ARCHITECTURE.md`. For features crossing 3+ modules, a migration, or an auth/workflow change, consider writing an ExecPlan first — see `.claude/PLANS.md` and copy `.claude/execplans/TEMPLATE.md`.

## Gotchas

- Docker Compose only starts the DB — run the Spring Boot app itself locally.
- MapStruct implementations only regenerate on compile — always `./mvnw compile` (or a full build) after touching a mapper interface.
- `devices.serial_number` has DB-level `NOT NULL` + uniqueness; duplicate/missing values fail on insert/update, and it's also `@NotBlank`-validated at the controller boundary.
- CORS is allowlist-based (`spring.security.cors.allowed-origins`) — a frontend host/port change needs this updated or requests fail before reaching a controller.
