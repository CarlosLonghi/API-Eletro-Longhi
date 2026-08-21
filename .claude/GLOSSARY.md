# Glossary — Domain Terminology

Deeper-dive reference for the domain model summarized in the root `CLAUDE.md`. Read that first; come here when you need entity-level detail.

## Core entities

### Brand
Device manufacturer. `id`, `name` (unique), `createdAt`, `updatedAt`. One Brand → many Devices. Small lookup table — listed via plain `GET /brand`, no pagination.

### Device
An item brought in for repair. `id`, `model`, `serialNumber` (`@NotBlank` + DB unique constraint), `brand` (FK, required), `accessories` (many-to-many), `createdAt`, `updatedAt`. A device can accumulate multiple `RepairOrder`s over its lifetime (as of `V13`), but only one at a time — see `RepairOrder` below.

### Accessory
Lookup table for repair parts/components (e.g. remote control, power cable). `id`, `name` (unique), `price`, `createdAt`, `updatedAt`. Many-to-many with `Device`. Listed via plain `GET /accessory`, no pagination.

### Customer
Top-level entity, not embedded in `RepairOrder`. `id`, `name`, `phone`, `email` (unique), `createdAt`, `updatedAt`. Own repository/controller; paginated + filterable listing (`name`, `email`, `phone`).

### RepairOrder
Service ticket tracking a device repair. `id`, `description`, `status` (enum, required), `customer` (`@ManyToOne`, required), `device` (`@ManyToOne`, required), `createdAt`, `updatedAt`.
- **Status is a workflow, not a free enum** (`entity/enums/RepairOrderStatus`):
  `AWAITING_EVALUATION → IN_EVALUATION → AWAITING_APPROVAL → APPROVED → AWAITING_PARTS → IN_REPAIR → REPAIR_COMPLETED → PAYMENT_RECEIVED → DEVICE_COLLECTED`.
  Each value carries a Portuguese `description` (e.g. `APPROVED` → "Aprovado").
- Status changes go through a dedicated `PATCH`-style endpoint using `RepairOrderStatusUpdateRequest` — don't update status via the general update endpoint.
- A device may have more than one repair order over time, but a new order for a device is only allowed once its previous order reached `DEVICE_COLLECTED`. This rule is enforced in `RepairOrderService`, **not** a DB constraint.
- Paginated + filterable listing (`status`, `customerId`, `deviceId`, `createdFrom`, `createdTo`).

### User
`entity/User implements UserDetails`. `id`, `name`, `email` (unique), `password` (hashed), `role` (`entity/enums/Role`: `ADMIN` | `USER`, defaults to `USER`), `createdAt`, `updatedAt`. Role maps to a `ROLE_<name>` Spring `GrantedAuthority`. No admin-management endpoint exists yet — promote a user to `ADMIN` manually in the DB. `ADMIN` is required for create/delete on `Brand`/`Accessory` and delete on `Customer`/`Device`/`RepairOrder` — see `config/SecurityConfig`.

### RefreshToken
`entity/RefreshToken` — persisted refresh tokens (`repository/RefreshTokenRepository`, `service/RefreshTokenService`). Minting a new one revokes all previous tokens for that user, so a user has at most one valid refresh token at a time.

## Architectural concepts

### JWT + refresh tokens
Stateless access tokens generated/verified by `config/TokenService`; claims extracted into `SecurityContext` by `config/SecurityFilter` on every request. Refresh tokens are a separate, longer-lived, DB-persisted credential used only to mint new access tokens via `POST /auth/refresh` (which also rotates the refresh token). See root `CLAUDE.md` "Auth" section for the endpoint contracts.

### DTO (record-based)
Request/response payloads are Java records under `controller/request` and `controller/response`. Controllers never return entities directly — always `mapper.toResponse(entity)`.

### Mapper (MapStruct)
`mapper/*` interfaces annotated `@Mapper(componentModel = "spring")`. Convention: `toEntity(RequestRecord) → Entity`, `toResponse(Entity) → ResponseRecord`. Some carry default helper methods for id → placeholder-entity resolution (e.g. `brandFromId`, `accessoryFromId`) and `List<Long> → List<Entity>` conversions. Implementations are generated at compile time — always `./mvnw compile` after touching a mapper interface.

### Flyway migration
`src/main/resources/db/migration/V*.sql`, applied automatically on startup, tracked in `flyway_schema_history`. Append-only — never edit an existing `V*.sql`. Confirm the current max version with `ls` before citing a number in docs or code; don't trust a hardcoded count (this file will go stale too).

### Specification (Spring Data JPA)
Dynamic predicate composition for the three paginated resources: `repository/specification/{Device,Customer,RepairOrder}Specification.java`. Optional filters only add predicates when the corresponding request param is present.

### Pagination
Shared across `Device`, `Customer`, `RepairOrder` listings: `page`, `size`, `sortBy`, `direction` params, built via `controller/support/PaginationUtils`. `Brand` and `Accessory` deliberately stay plain `List` — see `[[architecture]]` invariant I4.1.

## Quick lookups

| Term | File |
|------|------|
| Repair order workflow | `entity/enums/RepairOrderStatus.java` |
| Role enum | `entity/enums/Role.java` |
| Role-based authorization rules | `config/SecurityConfig.java` |
| Refresh token entity | `entity/RefreshToken.java` |
| JWT claims | `config/TokenService.java`, `config/JWTUserData.java` |
| Login throttling | `service/LoginAttemptService.java` |
