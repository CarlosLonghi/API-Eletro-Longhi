# 🏗️ ARCHITECTURE — System Design & Module Responsibilities

This document maps the Eletro Longhi codebase structure, explains each module's responsibility, and defines architectural invariants agents must respect.

---

## Directory Structure & Module Map

```
src/main/java/br/com/carloslonghi/eletrolonghi/
│
├── EletrolonghiApplication.java         # Spring Boot entry point
│
├── config/                              # Global configuration & cross-cutting concerns
│   ├── SecurityConfig.java              # Spring Security + JWT policy (stateless, public /auth/*)
│   ├── SecurityFilter.java              # HTTP filter: extracts JWT claims → SecurityContext
│   ├── TokenService.java                # Generate & verify JWT tokens (claims: userId, userName)
│   ├── ApplicationControllerAdvice.java # Global exception → HTTP error response translator
│   └── SwaggerConfig.java               # OpenAPI/Springdoc configuration
│
├── controller/                          # HTTP entry points; request/response DTO handling
│   ├── api/spec/                        # Swagger interface contracts (@Operation, @ApiResponse)
│   ├── request/                         # Request DTOs (Java records, validated with @NotNull/@NotBlank)
│   │   ├── DeviceRequest.java
│   │   ├── BrandRequest.java
│   │   ├── AccessoryRequest.java
│   │   ├── CustomerRequest.java
│   │   └── RepairOrderRequest.java
│   │
│   ├── response/                        # Response DTOs (Java records for HTTP serialization)
│   │   ├── DeviceResponse.java
│   │   ├── BrandResponse.java
│   │   ├── AccessoryResponse.java
│   │   ├── CustomerResponse.java
│   │   └── RepairOrderResponse.java
│   │
│   ├── DeviceController.java            # GET/POST/PUT/DELETE /device; paginated + advanced filters
│   ├── BrandController.java             # GET/POST/DELETE /brand
│   ├── AccessoryController.java         # GET/POST/DELETE /accessory
│   ├── CustomerController.java          # GET/POST/PUT/DELETE /customer; paginated + advanced filters
│   ├── RepairOrderController.java       # GET/POST/PUT/DELETE /repair-order; paginated + advanced filters
│   └── UserController.java              # POST /auth/register, POST /auth/login
│
├── service/                             # Business logic; coordinates repositories
│   ├── DeviceService.java               # find/create/update/delete Device; pageable/spec filter support
│   ├── BrandService.java                # find/create/delete Brand
│   ├── AccessoryService.java            # find/create/delete Accessory
│   ├── CustomerService.java             # find/create/update/delete Customer; pageable/spec filter support
│   ├── RepairOrderService.java          # find/create/update/delete RepairOrder; pageable/spec filter support
│   └── UserService.java                 # register, authenticate, generate JWT
│
├── repository/                          # Spring Data JPA repositories (queries → SQL)
│   ├── DeviceRepository.java            # findDevicesByBrandId(Long brandId)
│   ├── BrandRepository.java
│   ├── AccessoryRepository.java
│   ├── CustomerRepository.java
│   ├── RepairOrderRepository.java
│   ├── specification/                   # Dynamic filter builders (Spring Data Specification)
│   │   ├── DeviceSpecification.java
│   │   ├── CustomerSpecification.java
│   │   └── RepairOrderSpecification.java
│   └── UserRepository.java              # findByUserName(String)
│
├── entity/                              # JPA-annotated entities (database row representations)
│   ├── Device.java                      # @Entity; FK brand; M2M accessories
│   ├── Brand.java                       # @Entity; unique name
│   ├── Accessory.java                   # @Entity; unique name; price
│   ├── Customer.java                    # @Entity (or embedded in RepairOrder)
│   ├── RepairOrder.java                 # @Entity; unique device FK; embedded Customer; status enum
│   ├── User.java                        # @Entity; unique userName; password (hashed)
│   └── RepairOrderStatus.java           # Enum: OPEN, IN_PROGRESS, COMPLETED, CANCELED
│
├── mapper/                              # MapStruct: DTO ↔ Entity converters (Spring beans)
│   ├── DeviceMapper.java                # @Mapper(componentModel = "spring")
│   │   └── toEntity(DeviceRequest) → Device
│   │   └── toResponse(Device) → DeviceResponse
│   │   └── brandFromId(Long) → Brand (helper for ID → entity)
│   │   └── map(List<Long>) → List<Accessory> (helper for IDs → entities)
│   │
│   ├── BrandMapper.java
│   ├── AccessoryMapper.java
│   ├── CustomerMapper.java
│   ├── RepairOrderMapper.java
│   └── UserMapper.java
│
├── exception/                           # Custom exception types
│   └── (TBD: domain-specific exceptions if not handled in ApplicationControllerAdvice)
│
└── config/JWTUserData.java              # (in config) DTO for JWT claims (userId, userName)

src/main/resources/
├── application.properties                # Database URL, JWT secret, Flyway baseline
├── db/migration/
│   ├── V1__create_table_brands.sql      # CREATE TABLE brands
│   ├── V2__create_table_accessories.sql # CREATE TABLE accessories
│   ├── V3__create_table_devices.sql     # CREATE TABLE devices (FK brands)
│   ├── V4__create_table_devices_accessories.sql  # M2M join table
│   ├── V5__create_table_customers.sql   # CREATE TABLE customers
│   ├── V6__create_table_repair_orders.sql       # CREATE TABLE repair_orders
│   └── V7__create_table_users.sql       # CREATE TABLE users
```

---

## Module Responsibilities

### **config/**
**Purpose**: Global policies, security, error handling, and cross-cutting concerns.

| File | Responsibility |
|------|-----------------|
| `SecurityConfig.java` | Defines Spring Security policy: stateless; public `/auth/*`; protected everything else. |
| `SecurityFilter.java` | HTTP interceptor; extracts JWT from `Authorization: Bearer` header; sets `JWTUserData` in context. |
| `TokenService.java` | Generate JWT (claims: userId, userName, iat, exp); verify & extract claims. Uses `spring.security.secret`. |
| `ApplicationControllerAdvice.java` | Global `@ExceptionHandler`: MethodArgumentNotValidException → 400 + field errors; DataIntegrityViolationException → 409. |
| `SwaggerConfig.java` | OpenAPI/Springdoc configuration for Swagger UI at `/swagger-ui/index.html`. |

**Key Pattern**: Changes to security, validation, or error handling affect all endpoints; modify conservatively.

---

### **controller/**
**Purpose**: HTTP entry points; parse/validate requests; delegate to services; return responses.

**Structure**:
- **Request DTOs** (`controller/request/`) — Record types with `@NotNull`, `@NotBlank` annotations.
- **Response DTOs** (`controller/response/`) — Record types for JSON serialization.
- **Controllers** — Inject service + mapper beans; call `mapper.toEntity(request)` → `service.create(entity)` → `mapper.toResponse(entity)`.

**Key Patterns**:
1. Controllers **never return entities directly** — always map via `mapper.toResponse(entity)`.
2. Controllers use `@Valid` on request DTOs → validation errors handled by `ApplicationControllerAdvice`.
3. Single-entity lookups return `Optional<Entity>`; controllers call `ifPresentOrElse(...)` to decide 200 vs 404.
4. Deletion returns `ResponseEntity.noContent()` (204).
5. Creation returns `ResponseEntity.created(...)` (201) + response body.
6. Listing endpoints follow two profiles:
   - Simple lookup tables (`Brand`, `Accessory`) return `List<ResponseDTO>` without pagination.
   - Operational resources (`Device`, `Customer`, `RepairOrder`) return `Page<ResponseDTO>` with filters + sort/pagination params.

**Example Flow**:
```java
@PostMapping
public ResponseEntity<DeviceResponse> create(@Valid @RequestBody DeviceRequest request) {
    Device device = deviceMapper.toEntity(request);        // Request DTO → Entity
    Device saved = deviceService.create(device);           // Business logic
    return ResponseEntity.created(...).body(deviceMapper.toResponse(saved));  // Entity → Response DTO
}
```

---

### **service/**
**Purpose**: Business logic; data validation; repository coordination.

**Naming Convention**:
- `findById(Long) → Optional<Entity>` — Single lookup.
- `findAll() → List<Entity>` — Simple collection lookup (low-volume tables).
- `findAll(filters..., Pageable) → Page<Entity>` — Paginated + filtered listing.
- `findXxxByYyy(Type yyy) → List<Entity>` — Derived query helper when needed (e.g., `findDevicesByBrandId`).
- `create(Entity) → Entity` — Persist new entity.
- `update(Entity) → Entity` — Merge entity changes.
- `delete(Long) → void` — Remove entity.

**Key Patterns**:
1. Services **return Optional** for single lookups; controllers translate to HTTP status.
2. Services **throw exceptions** for business rule violations (e.g., "Cannot delete brand if devices exist"); `ApplicationControllerAdvice` catches and responds.
3. Services do **not** handle HTTP concerns (status codes, headers); only business rules.
4. Services compose dynamic list filters through `*Specification.withFilters(...)` for pageable endpoints.

**Example**:
```java
public Optional<Device> findById(Long id) {
    return deviceRepository.findById(id);  // Delegates to repository
}

public Device create(Device device) {
    // Business validation could go here
    return deviceRepository.save(device);
}
```

---

### **repository/**
**Purpose**: Spring Data JPA; queries translated to SQL.

**Naming Convention**:
- Extend `JpaRepository<Entity, Long>`.
- Add `JpaSpecificationExecutor<Entity>` for entities with advanced listing filters.
- Derived queries follow Spring Data naming: `findDevicesByBrandId(Long brandId)`.
- Use `@Query` only if derived query is too complex.

**Key Pattern**: Repositories are thin; business logic belongs in services, not in custom `@Query` methods.

**Example**:
```java
public interface DeviceRepository extends JpaRepository<Device, Long>, JpaSpecificationExecutor<Device> {
    List<Device> findDevicesByBrandId(Long brandId);  // Derived query
}
```

---

### **entity/**
**Purpose**: JPA-annotated POJOs representing database rows.

**Key Patterns**:
1. Entities are **mutable** (unlike DTOs).
2. Use `@Entity`, `@Table`, `@Column` for mapping.
3. Use `@ManyToOne`, `@OneToMany`, `@ManyToMany` for relationships.
4. Use `@CreationTimestamp` / `@UpdateTimestamp` for audit fields.
5. Use `@Enumerated(EnumType.STRING)` for enum fields (or VARCHAR for flexibility).

**Example**:
```java
@Entity
@Table(name = "devices")
public class Device {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "brand_id", nullable = false)
    private Brand brand;

    @ManyToMany
    @JoinTable(name = "devices_accessories", ...)
    private List<Accessory> accessories;
}
```

---

### **mapper/** (MapStruct)
**Purpose**: Convert between entities and DTOs.

**Convention**:
- Annotate with `@Mapper(componentModel = "spring")` to make it a Spring bean.
- Public method `toEntity(RequestRecord) → Entity`.
- Public method `toResponse(Entity) → ResponseRecord`.
- Default method `SomeEntity someEntityFromId(Long id)` for ID → entity resolution.

**Key Pattern**: MapStruct generates implementations at compile time; ensure `mapstruct-processor` is in `pom.xml`.

**Example**:
```java
@Mapper(componentModel = "spring")
public interface DeviceMapper {
    Device toEntity(DeviceRequest request);
    DeviceResponse toResponse(Device device);

    default Brand brandFromId(Long id) {
        if (id == null) return null;
        return new Brand(id, null, null, null);  // Placeholder; repository will load it
    }
}
```

---

## Architectural Invariants

### **I1: JWT is the only authentication mechanism**
- No sessions; all protected endpoints require a valid JWT in the `Authorization: Bearer` header.
- Public endpoints: `/auth/register`, `/auth/login`, `/swagger-ui/*`.
- **Agent Rule**: When adding endpoints, always require JWT unless explicitly marked as public.

### **I2: Controllers never expose entities directly**
- Always map entity → response DTO via mapper before returning.
- **Agent Rule**: If you see `return entity;` in a controller, it's a bug.

### **I3: Services return Optional for single-entity lookups**
- `findById` returns `Optional<Entity>`, not `Entity` or `null`.
- Controllers decide 200 vs 404 based on `isPresent()`.
- **Agent Rule**: Follow this for consistency; don't return `null`.

### **I4: Repositories use Spring Data naming conventions**
- Derived queries like `findDevicesByBrandId` instead of custom `@Query`.
- Improves readability and reduces SQL injection risk.
- **Agent Rule**: Use derived queries first; combine with `Specification` for dynamic filters; only use `@Query` if necessary.

### **I4.1: Listing strategy is intentionally selective**
- `Brand` and `Accessory` are small lookup entities and should stay as plain list endpoints unless requirements change.
- `Device`, `Customer`, and `RepairOrder` use pagination (`Pageable`) and dynamic filtering (`Specification`).
- **Agent Rule**: Keep this split unless product explicitly asks for a different listing profile.

### **I5: Validation happens at controller boundary**
- `@Valid` on request DTOs; annotations like `@NotNull`, `@NotBlank`.
- `ApplicationControllerAdvice` translates validation errors to 400 + error map.
- **Agent Rule**: Don't duplicate validation in services; trust the controller.

### **I6: Deletion returns 204 No Content**
- Not 200; no response body.
- **Agent Rule**: Use `ResponseEntity.noContent().build()`.

### **I7: Database migrations are append-only**
- Never modify old migrations; always add new `V*.sql` files.
- **Agent Rule**: When changing schema, create `V{N+1}__*.sql`; don't edit `V{N}__*.sql`.

### **I8: Flyway baseline runs automatically**
- App startup runs pending migrations; tracked in `flyway_schema_history`.
- **Agent Rule**: Ensure `spring.flyway.baseline-on-migrate=true` in `application.properties`.

---

## Data Flow Diagram

```
HTTP Request
    ↓
[SecurityFilter] ← Extract JWT claims → SecurityContext
    ↓
[Controller] ← Accept @Valid RequestDTO
    ↓
[Mapper.toEntity] ← RequestDTO → Entity
    ↓
[Service] ← Business logic (find/create/update/delete)
    ↓
[Repository] ← SQL query via Spring Data JPA
    ↓
[PostgreSQL] ← Persist/retrieve
    ↓
[Service] ← Return Optional<Entity> or List<Entity>
    ↓
[Mapper.toResponse] ← Entity → ResponseDTO
    ↓
[Controller] ← Return ResponseEntity with status/body
    ↓
[ApplicationControllerAdvice] ← Exception handling
    ↓
HTTP Response (200/201/400/403/404/409)
```

---

## Adding a New Feature: Step-by-Step

### **Example: Add "Device Status" field to track repair urgency**

1. **Update Entity** (`entity/Device.java`):
   - Add `@Enumerated(EnumType.STRING) private DeviceStatus status;`

2. **Create Flyway Migration** (`V8__add_device_status.sql`):
   - `ALTER TABLE devices ADD COLUMN status VARCHAR(50) DEFAULT 'PENDING';`

3. **Update Request DTO** (`controller/request/DeviceRequest.java`):
   - Add `DeviceStatus status` field with `@NotNull`.

4. **Update Response DTO** (`controller/response/DeviceResponse.java`):
   - Add `DeviceStatus status` field.

5. **Update Mapper** (`mapper/DeviceMapper.java`):
   - MapStruct auto-maps the new field; no manual code needed.

6. **Update Service** (`service/DeviceService.java`):
   - Add any business logic (e.g., validation, default values).

7. **Update Controller** (`controller/DeviceController.java`):
   - No change needed if using standard mappers.

8. **Test**:
   - Run `./mvnw compile` to generate MapStruct implementations.
   - Run `./mvnw spring-boot:run` to apply migration + start app.

---

## Key Files to Know

| Scenario | Key Files |
|----------|-----------|
| Add new entity | `entity/*.java`, `mapper/*.java`, `V*.sql` |
| Add new endpoint | `controller/DeviceController.java`, `service/DeviceService.java`, `repository/DeviceRepository.java` |
| Change security | `config/SecurityConfig.java`, `config/SecurityFilter.java`, `config/TokenService.java` |
| Handle error | `config/ApplicationControllerAdvice.java` |
| Add validation | `controller/request/*.java` (annotations) → auto-handled by advice |
| Query by filter | `repository/specification/*.java` + `JpaSpecificationExecutor` repos → `service/*Service.java` → `controller/*Controller.java` |

---

**Last updated**: 2026-07-20  
**Version**: 1.1

