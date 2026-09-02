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

### Payment
The payment of a repair order. `id`, `amount` (`BigDecimal`, `NUMERIC(12,2)`), `method` (`entity/enums/PaymentMethod`: `CASH` | `CARD` | `PIX` | `BOLETO`), `status` (`entity/enums/PaymentStatus`: `PENDING` | `APPROVED` | `REJECTED` | `REFUNDED` | `CANCELLED`), `installments` (>1 only for `CARD`; `PaymentService` normalizes it to 1 otherwise), `description`, `payerName`/`payerDocument` (printed on the receipt), `externalReference`/`gatewayPaymentId` (nullable, reserved for the future Mercado Pago integration), `repairOrder` (`@ManyToOne`, required), `paidAt`, `createdAt`, `updatedAt`.
- **One payment per repair order** — `repair_order_id` is `UNIQUE NOT NULL`; a second payment for the same order is rejected in `PaymentService` (`PaymentAlreadyExistsForRepairOrderException` → 422), see `[[architecture]]` invariant I10.
- `PaymentStatus` is **not** a strict workflow. Moving to `APPROVED` (create or `PATCH /payment/{id}/status`) stamps `paidAt` and calls `RepairOrderService.markPaymentReceived` — advances the order `REPAIR_COMPLETED → PAYMENT_RECEIVED`, no-op + log otherwise.
- `GET /payment/{id}/receipt` → non-fiscal PDF receipt (`service/PaymentReceiptService`, OpenPDF), store data from `config/ShopProperties` (`shop.*`).
- Paginated + filterable listing (`status`, `method`, `repairOrderId`, `createdFrom`, `createdTo`). `DELETE /payment/{id}` is ADMIN-only.
- `client/MercadoPagoClient` is a **skeleton** for the future physical-card-machine integration (Mercado Pago **Point** API; confirmation by webhook once hosted, or polling). Only `getPayment` (polling) is implemented; `mercadopago.*` config lives in `config/MercadoPagoProperties`.

### User
`entity/User implements UserDetails`. `id`, `name`, `email` (unique), `password` (hashed), `role` (`entity/enums/Role`: `ADMIN` | `USER`, defaults to `USER`), `enabled` (defaults `true` at the entity/DB level, but `UserService.save` forces it `false` on every self-registration), `createdAt`, `updatedAt`. Role maps to a `ROLE_<name>` Spring `GrantedAuthority`. Admin management: `GET /user` (paginated + filterable by `name`/`email`/`role`/`enabled`), `PATCH /user/{id}/role` (role-only), `PATCH /user/{id}/status` (enabled-only). `ADMIN` is required for create/delete on `Brand`/`Accessory`, delete on `Customer`/`Device`/`RepairOrder`/`Payment`, and all three `/user` admin endpoints above — see `config/SecurityConfig`.

### RefreshToken
`entity/RefreshToken` — persisted refresh tokens (`repository/RefreshTokenRepository`, `service/RefreshTokenService`). Minting a new one revokes all previous tokens for that user, so a user has at most one valid refresh token at a time.

## Architectural concepts

### JWT + refresh tokens
Stateless access tokens generated/verified by `config/TokenService`; claims extracted into `SecurityContext` by `config/SecurityFilter` on every request. Refresh tokens are a separate, longer-lived, DB-persisted credential used only to mint new access tokens via `POST /auth/refresh` (which also rotates the refresh token). See root `CLAUDE.md` "Auth" section for the endpoint contracts.

### Account activation & suspension
A self-registered user (`POST /auth/register`) starts `enabled=false` — forced in `UserService.save`, regardless of the entity's own `true` default — and cannot log in until an ADMIN both activates it (`PATCH /user/{id}/status`, `enabled:true`) and sets its role (`PATCH /user/{id}/role`). A disabled user's login attempt fails via Spring Security's built-in `DisabledException`, caught in `AuthController.login` and translated to `exception/AccountNotActivatedException` (403). Suspending an active user reuses the same `PATCH /user/{id}/status` endpoint with `enabled:false`. The effect is **not instant**: `SecurityFilter` still trusts JWT claims only (no per-request DB lookup — see "JWT + refresh tokens" above), so an already-issued access token stays valid until it expires. `RefreshTokenService.findValidToken` checks `user.isEnabled()`, so the refresh token stops working immediately, and a new login attempt is rejected immediately — only the current, still-live access token has a delayed cutoff.

### DTO (record-based)
Request/response payloads are Java records under `controller/request` and `controller/response`. Controllers never return entities directly — always `mapper.toResponse(entity)`.

### Mapper (MapStruct)
`mapper/*` interfaces annotated `@Mapper(componentModel = "spring")`. Convention: `toEntity(RequestRecord) → Entity`, `toResponse(Entity) → ResponseRecord`. Some carry default helper methods for id → placeholder-entity resolution (e.g. `brandFromId`, `accessoryFromId`) and `List<Long> → List<Entity>` conversions. Implementations are generated at compile time — always `./mvnw compile` after touching a mapper interface.

### Flyway migration
`src/main/resources/db/migration/V*.sql`, applied automatically on startup, tracked in `flyway_schema_history`. Append-only — never edit an existing `V*.sql`. Confirm the current max version with `ls` before citing a number in docs or code; don't trust a hardcoded count (this file will go stale too).

### Specification (Spring Data JPA)
Dynamic predicate composition for the paginated resources: `repository/specification/{Device,Customer,RepairOrder,Payment,User}Specification.java`. Optional filters only add predicates when the corresponding request param is present.

### Pagination
Shared across `Device`, `Customer`, `RepairOrder`, `Payment`, `User` listings: `page`, `size`, `sortBy`, `direction` params, built via `controller/support/PaginationUtils`. `Brand` and `Accessory` deliberately stay plain `List` — see `[[architecture]]` invariant I4.1.

## Quick lookups

| Term | File |
|------|------|
| Repair order workflow | `entity/enums/RepairOrderStatus.java` |
| Payment method / status enums | `entity/enums/PaymentMethod.java`, `entity/enums/PaymentStatus.java` |
| Payment → order auto-advance | `RepairOrderService.markPaymentReceived` |
| Payment receipt (PDF) | `service/PaymentReceiptService.java`, `config/ShopProperties.java` |
| Mercado Pago client (skeleton) | `client/MercadoPagoClient.java`, `config/MercadoPagoProperties.java` |
| Role enum | `entity/enums/Role.java` |
| Role-based authorization rules | `config/SecurityConfig.java` |
| User admin endpoints (list/role/status) | `controller/UserController.java` |
| Account activation exception | `exception/AccountNotActivatedException.java` |
| Refresh token entity | `entity/RefreshToken.java` |
| JWT claims | `config/TokenService.java`, `config/JWTUserData.java` |
| Login throttling | `service/LoginAttemptService.java` |
