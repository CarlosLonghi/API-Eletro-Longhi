# Architecture — Module Map & Invariants

Deeper-dive reference for the layered architecture summarized in the root `CLAUDE.md`. Read that first; come here for the full module map and the invariants list.

## Directory structure

```
src/main/java/br/com/carloslonghi/eletrolonghi/
├── EletrolonghiApplication.java
│
├── config/
│   ├── SecurityConfig.java              # Stateless JWT policy; public /auth/*; ADMIN/GERENTE create/delete on Brand/Accessory + delete on Customer/Device/RepairOrder (soft), Payment (hard); TECNICO+ for status; ADMIN-only /user; denyAll catch-all
│   ├── SecurityFilter.java              # Extracts JWT claims → SecurityContext
│   ├── TokenService.java                # Generate/verify access tokens
│   ├── JWTUserData.java                 # DTO for decoded JWT claims
│   ├── ApplicationControllerAdvice.java # Global exception → HTTP translator
│   ├── ShopProperties.java              # shop.* — store data for the payment receipt
│   ├── MercadoPagoProperties.java       # mercadopago.* — access token / base URL for the Checkout Pro client (Point creds still unused)
│   └── SwaggerConfig.java               # OpenAPI/Springdoc config
│
├── client/
│   ├── MercadoPagoClient.java           # HTTP client: Checkout Pro preference + payment search/get (Point API still TODO)
│   └── dto/                             # GatewayPaymentSnapshot, CheckoutPreference, Preference{Request,Item,Payer,Identification}, PaymentSearchResponse
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
│   ├── PaymentController.java           # /payment — CRUD + status PATCH + /{id}/checkout + /{id}/sync + /{id}/receipt (PDF)
│   └── UserController.java              # /user — paginated + filters (ADMIN); role PATCH + status PATCH (ADMIN)
│
├── service/
│   ├── BrandService.java / AccessoryService.java     # simple CRUD
│   ├── DeviceService.java / CustomerService.java      # CRUD + Pageable/Specification filters
│   ├── RepairOrderService.java          # CRUD + filters + status-workflow + "one open order per device" rule + "paid before collected" guard
│   ├── PaymentService.java              # CRUD + filters + "one payment per order" + "order budget approved first" + paidAt stamp on APPROVED + Checkout Pro link/sync
│   ├── PaymentReceiptService.java       # non-fiscal PDF receipt (OpenPDF)
│   ├── AuthService.java                 # UserDetailsService (login-time user lookup)
│   ├── UserService.java                 # registration (forces enabled=false), role/status updates, filtered listing
│   ├── LoginAttemptService.java         # in-memory brute-force throttling
│   └── RefreshTokenService.java         # create/rotate/revoke refresh tokens
│
├── repository/
│   ├── {Brand,Accessory,Device,Customer,RepairOrder,Payment,User,RefreshToken}Repository.java
│   └── specification/{Device,Customer,RepairOrder,Payment,User}Specification.java
│
├── entity/
│   ├── {Brand,Accessory,Device,Customer,RepairOrder,Payment,User,RefreshToken}.java
│   └── enums/{RepairOrderStatus,PaymentStatus,PaymentMethod,Role}.java
│
└── mapper/
    └── {Brand,Accessory,Device,Customer,RepairOrder,Payment,User}Mapper.java  # MapStruct, componentModel=spring

src/main/resources/
├── application.properties
└── db/migration/V1..V20__*.sql   # confirm actual max with `ls` — append-only
```

## Module responsibilities

**config/** — Global policy: security, error translation, Swagger. Touching these affects every endpoint; change conservatively.

**controller/** — Parse/validate requests, delegate to service, map to response DTO. Controllers implement `controller/api/spec/*Api.java` interfaces — update the interface signature/docs in the same change as the controller method.

**service/** — Business rules and repository coordination. Returns `Optional<Entity>` for single lookups; throws for business-rule violations (e.g. RepairOrder workflow ordering), which `ApplicationControllerAdvice` translates to HTTP responses. Does not deal in HTTP status codes directly.

**repository/** — Spring Data JPA. Derived-query naming first (`findDevicesByBrandId`); `JpaSpecificationExecutor` + `repository/specification/*` for the three paginated/filterable resources; `@Query` only when a derived query would be unreasonably complex.

**entity/** — Mutable JPA POJOs. `@Enumerated(EnumType.STRING)` for enums, `@CreationTimestamp`/`@UpdateTimestamp` for audit columns.

**mapper/** — MapStruct interfaces, Spring beans. `toEntity(RequestRecord)` / `toResponse(Entity)`, plus default helper methods for id → placeholder-entity resolution. Never hand-write a static mapper utility class — MapStruct generates the implementation at compile time.

## Architectural invariants

- **I1 — JWT is the only auth mechanism.** No sessions. Public endpoints: `/auth/register`, `/auth/login`, `/auth/refresh`, `/auth/logout`, Swagger paths. Authorization is entirely URL-based in `SecurityConfig` (no `@PreAuthorize`/method security, no `RoleHierarchy`) — each `requestMatchers(...)` rule lists every allowed role via `hasAnyRole(...)`, and the chain ends with **`.anyRequest().denyAll()`** so a role with no matching rule (notably `PENDENTE`) is denied. Roles (`entity/enums/Role`): `ADMIN` (all), `GERENTE` (all except the `/user` management endpoints; its deletes are soft like everyone's), `ATENDENTE` (marcas/acessórios read-only; full CRUD-minus-delete on `Customer`/`Device`/`RepairOrder`; full `Payment` incl. checkout/sync; no status change), `TECNICO` (read `RepairOrder` + `PATCH /repair-order/{id}/status` only), `PENDENTE` (nothing). `POST`/`DELETE` on `Brand`/`Accessory` and `DELETE` on `Customer`/`Device`/`RepairOrder`/`Payment` → `ADMIN`/`GERENTE`; `PATCH /repair-order/{id}/status` → `TECNICO`/`GERENTE`/`ADMIN`; `GET /user` + `PATCH /user/{id}/role` + `PATCH /user/{id}/status` → `ADMIN` only. A new endpoint must add its own `requestMatchers(...).hasAnyRole(...)` rule before `anyRequest().denyAll()` (or it 403s for everyone), and document the roles in the operation's `*Api.java` `@Operation`/`@ApiResponse` 403 text.
- **I1.1 — API deletion is a soft delete (except `Payment`).** `Brand`, `Accessory`, `Customer`, `Device` and `RepairOrder` carry `@SoftDelete(strategy = TIMESTAMP, columnName = "deleted_at")`; `deleteById` stamps `deleted_at` and all reads (derived queries, `Specification`, `@EntityGraph`, the `devices_accessories` join table) get `deleted_at IS NULL` appended. There is **no API endpoint for permanent removal** — that is a manual DB operation. Services reject soft-deleting a record with active children (`EntityInUseException` → 409): `Brand`←`Device`, `Accessory`←`Device`, `Customer`/`Device`←`RepairOrder`, `RepairOrder`←`Payment`. **`Payment` is hard-deleted**: it is the inverse side of `RepairOrder`'s eager `@OneToOne`, so `@SoftDelete` on it makes Hibernate throw `FetchNotFoundException` for every payment-less repair order, and its `UNIQUE repair_order_id` would let a soft-deleted row block a replacement; `PaymentService.deleteById` nulls `RepairOrder.payment` then removes the row. A new deletable entity normally adds `@SoftDelete`, a `V{n}` migration for its `deleted_at` column (plus any owned collection table) and, if it can be referenced, an in-use guard — unless it sits on the inverse side of an eager to-one like `Payment`.
- **I9 — Self-registered accounts start disabled.** `UserService.save` forces `enabled=false` regardless of the `User` entity's own `true` default (which exists so pre-existing/seed rows aren't retroactively locked out by the `V15` migration). An ADMIN activates via `PATCH /user/{id}/status` (`enabled:true`) and sets the role via `PATCH /user/{id}/role`. A disabled user's login is rejected via Spring Security's `DisabledException` → `exception/AccountNotActivatedException` → 403 (`AuthController.login` / `ApplicationControllerAdvice`). Suspension (same `PATCH /user/{id}/status`, `enabled:false`) is not instant on already-issued access tokens — `SecurityFilter` trusts JWT claims only — but `RefreshTokenService.findValidToken` checks `user.isEnabled()`, so refresh and new logins are cut off immediately. See `[[glossary]]` "Account activation & suspension".
- **I2 — Controllers never expose entities.** Always map via `mapper.toResponse(entity)`. `return entity;` in a controller is a bug.
- **I3 — Services return `Optional` for single-entity lookups.** Controllers decide 200 vs 404 from `isPresent()`/`ifPresentOrElse`. Never return `null`.
- **I4 — Repositories use Spring Data derived-query naming first.** Reach for `Specification` before a custom `@Query`.
- **I4.1 — Listing strategy is intentionally split.** `Brand`/`Accessory` stay plain `List` (small lookup tables); `Device`/`Customer`/`RepairOrder`/`Payment`/`User` return `Page` with `Pageable` + `Specification` filters. Keep this split unless product requirements explicitly change it.
- **I5 — Validation happens at the controller boundary.** `@Valid` + Bean Validation annotations on request records; `ApplicationControllerAdvice` maps `MethodArgumentNotValidException` → 400 + field-errors map. Don't duplicate validation in services.
- **I6 — Deletion returns 204 No Content.** `ResponseEntity.noContent().build()`, no body.
- **I7 — Migrations are append-only.** Never edit an existing `V*.sql`; always add `V{n+1}`. Confirm the actual current max version with `ls` before citing one.
- **I8 — RepairOrder status is a workflow, not a free enum.** Transitions and the "one active order per device" rule live in `RepairOrderService`, not the DB. Status changes go **only** through `PATCH /repair-order/{id}/status` (`RepairOrderStatusUpdateRequest`, roles `TECNICO`/`GERENTE`/`ADMIN`); the general `PUT /repair-order/{id}` ignores the body's `status`. `→ DEVICE_COLLECTED` additionally requires the linked `Payment` to be `APPROVED` (`RepairOrderNotPaidException` → 422), checked in `updateStatus`.
- **I10 — One payment per repair order.** `Payment.repairOrder` is `@OneToOne` and `repair_order_id` is `UNIQUE NOT NULL`; `PaymentService.save` rejects a duplicate (`PaymentAlreadyExistsForRepairOrderException` → 422). It also rejects a payment for an order whose status is still before `APPROVED` in the workflow (`RepairOrderNotApprovedForPaymentException` → 422) — no charging before the budget is approved. `RepairOrder` reads it back via `@OneToOne(mappedBy = "repairOrder")` and the listing query pulls it in with `@EntityGraph`, exposing `paymentStatus` + `paymentId` on `RepairOrderResponse` (both null when there's no payment). `PaymentStatus` is **not** a strict workflow (unlike `RepairOrderStatus`); moving a payment to `APPROVED` (via create or the `PATCH /payment/{id}/status` endpoint) only stamps `paidAt` — the repair order's own status is never changed by a payment. Mercado Pago **Checkout Pro** payments (`method=MERCADO_PAGO_CHECKOUT`) get a link via `POST /payment/{id}/checkout` and are reconciled by manual polling via `POST /payment/{id}/sync` (no webhook — app not hosted).

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
