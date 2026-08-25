# Architecture — Module Map & Invariants

Deeper-dive reference for the layered architecture summarized in the root `CLAUDE.md`. Read that first; come here for the full module map and the invariants list.

## Directory structure

```
src/main/java/br/com/carloslonghi/eletrolonghi/
├── EletrolonghiApplication.java
│
├── config/
│   ├── SecurityConfig.java              # Stateless JWT policy; public /auth/*; ADMIN-only create/delete on Brand/Accessory, delete on Customer/Device/RepairOrder
│   ├── SecurityFilter.java              # Extracts JWT claims → SecurityContext
│   ├── TokenService.java                # Generate/verify access tokens
│   ├── JWTUserData.java                 # DTO for decoded JWT claims
│   ├── ApplicationControllerAdvice.java # Global exception → HTTP translator
│   └── SwaggerConfig.java               # OpenAPI/Springdoc config
│
├── controller/
│   ├── api/spec/                        # OpenAPI contract interfaces (*Api.java)
│   ├── request/                         # Request DTOs (records, @Valid-annotated)
│   ├── response/                        # Response DTOs (records)
│   ├── support/PaginationUtils.java     # Pageable + sort-direction builder
│   ├── AuthController.java              # /auth/register, /login, /refresh, /logout
│   ├── BrandController.java             # /brand — plain list
│   ├── AccessoryController.java         # /accessory — plain list
│   ├── DeviceController.java            # /device — paginated + filters
│   ├── CustomerController.java          # /customer — paginated + filters
│   ├── RepairOrderController.java       # /repair-order — paginated + filters + status PATCH
│   └── UserController.java              # /user — paginated + filters (ADMIN); role PATCH + status PATCH (ADMIN)
│
├── service/
│   ├── BrandService.java / AccessoryService.java     # simple CRUD
│   ├── DeviceService.java / CustomerService.java      # CRUD + Pageable/Specification filters
│   ├── RepairOrderService.java          # CRUD + filters + status-workflow + "one open order per device" rule
│   ├── AuthService.java                 # UserDetailsService (login-time user lookup)
│   ├── UserService.java                 # registration (forces enabled=false), role/status updates, filtered listing
│   ├── LoginAttemptService.java         # in-memory brute-force throttling
│   └── RefreshTokenService.java         # create/rotate/revoke refresh tokens
│
├── repository/
│   ├── {Brand,Accessory,Device,Customer,RepairOrder,User,RefreshToken}Repository.java
│   └── specification/{Device,Customer,RepairOrder,User}Specification.java
│
├── entity/
│   ├── {Brand,Accessory,Device,Customer,RepairOrder,User,RefreshToken}.java
│   └── enums/{RepairOrderStatus,Role}.java
│
└── mapper/
    └── {Brand,Accessory,Device,Customer,RepairOrder,User}Mapper.java  # MapStruct, componentModel=spring

src/main/resources/
├── application.properties
└── db/migration/V1..V13__*.sql   # confirm actual max with `ls` — append-only
```

## Module responsibilities

**config/** — Global policy: security, error translation, Swagger. Touching these affects every endpoint; change conservatively.

**controller/** — Parse/validate requests, delegate to service, map to response DTO. Controllers implement `controller/api/spec/*Api.java` interfaces — update the interface signature/docs in the same change as the controller method.

**service/** — Business rules and repository coordination. Returns `Optional<Entity>` for single lookups; throws for business-rule violations (e.g. RepairOrder workflow ordering), which `ApplicationControllerAdvice` translates to HTTP responses. Does not deal in HTTP status codes directly.

**repository/** — Spring Data JPA. Derived-query naming first (`findDevicesByBrandId`); `JpaSpecificationExecutor` + `repository/specification/*` for the three paginated/filterable resources; `@Query` only when a derived query would be unreasonably complex.

**entity/** — Mutable JPA POJOs. `@Enumerated(EnumType.STRING)` for enums, `@CreationTimestamp`/`@UpdateTimestamp` for audit columns.

**mapper/** — MapStruct interfaces, Spring beans. `toEntity(RequestRecord)` / `toResponse(Entity)`, plus default helper methods for id → placeholder-entity resolution. Never hand-write a static mapper utility class — MapStruct generates the implementation at compile time.

## Architectural invariants

- **I1 — JWT is the only auth mechanism.** No sessions. Public endpoints: `/auth/register`, `/auth/login`, `/auth/refresh`, `/auth/logout`, Swagger paths. New endpoints require auth unless explicitly marked public in `SecurityConfig`. URL-based role rules (no `@PreAuthorize`/method security in this codebase) additionally require `ADMIN` for: `POST`/`DELETE /brand`, `POST`/`DELETE /accessory`, `DELETE /customer/{id}`, `DELETE /device/{id}`, `DELETE /repair-order/{id}`, `GET /user`, `PATCH /user/{id}/role`, `PATCH /user/{id}/status`. New ADMIN-only endpoints must add a `requestMatchers(...).hasRole("ADMIN")` rule in `SecurityConfig` before `anyRequest().authenticated()`, and document the restriction in the operation's `*Api.java` `@Operation`/`@ApiResponse` 403 text.
- **I9 — Self-registered accounts start disabled.** `UserService.save` forces `enabled=false` regardless of the `User` entity's own `true` default (which exists so pre-existing/seed rows aren't retroactively locked out by the `V15` migration). An ADMIN activates via `PATCH /user/{id}/status` (`enabled:true`) and sets the role via `PATCH /user/{id}/role`. A disabled user's login is rejected via Spring Security's `DisabledException` → `exception/AccountNotActivatedException` → 403 (`AuthController.login` / `ApplicationControllerAdvice`). Suspension (same `PATCH /user/{id}/status`, `enabled:false`) is not instant on already-issued access tokens — `SecurityFilter` trusts JWT claims only — but `RefreshTokenService.findValidToken` checks `user.isEnabled()`, so refresh and new logins are cut off immediately. See `[[glossary]]` "Account activation & suspension".
- **I2 — Controllers never expose entities.** Always map via `mapper.toResponse(entity)`. `return entity;` in a controller is a bug.
- **I3 — Services return `Optional` for single-entity lookups.** Controllers decide 200 vs 404 from `isPresent()`/`ifPresentOrElse`. Never return `null`.
- **I4 — Repositories use Spring Data derived-query naming first.** Reach for `Specification` before a custom `@Query`.
- **I4.1 — Listing strategy is intentionally split.** `Brand`/`Accessory` stay plain `List` (small lookup tables); `Device`/`Customer`/`RepairOrder`/`User` return `Page` with `Pageable` + `Specification` filters. Keep this split unless product requirements explicitly change it.
- **I5 — Validation happens at the controller boundary.** `@Valid` + Bean Validation annotations on request records; `ApplicationControllerAdvice` maps `MethodArgumentNotValidException` → 400 + field-errors map. Don't duplicate validation in services.
- **I6 — Deletion returns 204 No Content.** `ResponseEntity.noContent().build()`, no body.
- **I7 — Migrations are append-only.** Never edit an existing `V*.sql`; always add `V{n+1}`. Confirm the actual current max version with `ls` before citing one.
- **I8 — RepairOrder status is a workflow, not a free enum.** Transitions and the "one active order per device" rule live in `RepairOrderService`, not the DB. Status changes go through the dedicated `RepairOrderStatusUpdateRequest` endpoint, not the general update endpoint.

## Data flow

```
HTTP Request
    → [SecurityFilter] JWT claims → SecurityContext
    → [Controller] @Valid RequestDTO
    → [Mapper.toEntity] RequestDTO → Entity
    → [Service] business rules
    → [Repository] Spring Data JPA / Specification
    → PostgreSQL
    → [Service] Optional<Entity> / Page<Entity>
    → [Mapper.toResponse] Entity → ResponseDTO
    → [Controller] ResponseEntity
    → [ApplicationControllerAdvice] exception → error response
HTTP Response (200/201/204/400/401/404/409)
```

## Adding a new feature — checklist

1. **Entity** — add the field/relationship (`entity/*.java`).
2. **Migration** — next `V{n}__*.sql`, confirmed against actual `ls db/migration` output.
3. **Request/response DTOs** — add fields with validation annotations where needed.
4. **Mapper** — usually auto-maps; add a default helper only for id → entity resolution.
5. **API contract** — update `controller/api/spec/*Api.java` alongside the controller method.
6. **Service** — add business rules if any; keep HTTP concerns out.
7. **Compile** — `./mvnw compile` to regenerate MapStruct implementations.
8. **Tests** — mirror the change under `src/test/java/.../{config,controller,service}`; add a repository integration test if the query logic is non-trivial. Run `./mvnw verify` for the JaCoCo gate.

For anything crossing 3+ modules, a DB migration, or an auth/workflow change, consider writing an ExecPlan first — see `[[plans]]`.
