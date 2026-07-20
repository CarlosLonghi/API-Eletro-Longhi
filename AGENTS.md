# AGENTS — Quick onboarding for AI coding agents

Checklist for an agent run:
- Understand the Spring Boot layered structure (controller → service → repository).
- Locate security and token logic before changing auth flows (`config/TokenService`, `config/SecurityFilter`, `config/SecurityConfig`).
- Read DTOs (records) and mappers before changing endpoints (`controller/request`, `controller/response`, `mapper/*`).
- Verify DB migrations and properties before touching schema (`src/main/resources/db/migration` and `application.properties`).

Big picture (what matters)
- Language & framework: Java 21 + Spring Boot 3. Project is organized by feature packages under `br.com.carloslonghi.eletrolonghi` (config, controller, service, repository, entity, mapper).
- Flow: HTTP controllers accept record-based DTOs → controllers call Service layer (business rules) → Services use Spring Data JPA repositories → Entities persisted in PostgreSQL. Mappers convert between records and entities (see `mapper/DeviceMapper`).
- Security: JWT-based. Token generation & verification live in `config/TokenService` and are enforced by `config/SecurityFilter`. Security policy is configured in `config/SecurityConfig` (stateless; `/auth/register` and `/auth/login` are public).

Project-specific conventions — important for automated edits
- DTOs are Java records (e.g. `controller/request/DeviceRequest.java`). Expect accessor methods like `request.brand()` and immutable DTOs.
- Mappers: migrated to MapStruct (see `mapper/*`). Mappers are now Spring beans (`@Mapper(componentModel = "spring")`) and should be injected where used (controllers, services). The public methods follow the new convention:
  - `toEntity(RequestRecord)` — maps request DTO → entity
  - `toResponse(Entity)` — maps entity → response DTO
  MapStruct generates implementations; do not keep the old static utility classes. Some mappers contain helper default methods to convert id → entity placeholders (e.g. `brandFromId`, `accessoryFromId`) and explicit List<Long> → List<Accessory> helpers to match previous behavior.
- Services return Optional for single-entity lookups. Collection reads are split by endpoint profile: small lookup tables (`Brand`, `Accessory`) use plain `List`, while operational listings (`Device`, `Customer`, `RepairOrder`) support `Pageable` + filters and return `Page`.
- Repositories use Spring Data naming conventions for queries (example: `DeviceRepository.findDevicesByBrandId(Long brandId)`). Advanced listing filters use `JpaSpecificationExecutor` + `repository/specification/*`.
- Validation and error handling: controllers use `@Valid` on records; global exception translation is in `config/ApplicationControllerAdvice` (maps MethodArgumentNotValidException → field errors map, DataIntegrityViolationException → 409). When adding new validations, ensure messages match existing mapping patterns.

Build / run / debug workflows (commands and gotchas)
- Build package: `./mvnw -q -DskipTests package` or full build `./mvnw package`.
- Run locally: `./mvnw spring-boot:run`. The app reads `src/main/resources/application.properties` for DB and JWT secret.
- Docker: repository includes `docker-compose.yml` (defines `db` service using postgres:16). Note: current compose exposes only the DB; the app isn't containerized here — run the app locally or add a Dockerfile to containerize it.
- Database: Flyway migrations are in `src/main/resources/db/migration` (V1..V7). Before running locally, create the DB (README shows `CREATE DATABASE eletro_longhi;`) or use docker compose to start Postgres.
- Tests: standard Maven tests `./mvnw test`. There are currently no extensive test suites in repo (README lists tests as future work).

## 📦 External Dependencies & Integration Points

- **PostgreSQL**: JDBC URL in `application.properties`; Flyway migrations apply at startup.
- **JWT**: Auth0 Java JWT library; secret from `spring.security.secret` property (set in `application.properties` or env).
- **OpenAPI/Swagger**: Springdoc-enabled; UI at `/swagger-ui/index.html`.
- **MapStruct**: Generates code at compile time; processor configured in `pom.xml`.

---

## 💡 Quick Examples

### Add a new endpoint: "Get devices by brand"
1. **Repository** (`repository/DeviceRepository.java`): Add `List<Device> findDevicesByBrandId(Long brandId);`
2. **Service** (`service/DeviceService.java`): Add `public List<Device> findDevicesByBrandId(Long brandId) { return repository.findDevicesByBrandId(brandId); }`
3. **Controller** (`controller/DeviceController.java`): Expose `@GetMapping("/search")` with `brandId` + pagination/sort params and return `ResponseEntity<Page<DeviceResponse>>`.

### Listing strategy (current)
- **Simple list (no pagination/filter)**:
  - `GET /brand`
  - `GET /accessory`
- **Paginated + advanced filters** (shared params: `page`, `size`, `sortBy`, `direction`):
  - `GET /device` and `GET /device/search` (filters: `model`, `serialNumber`, `brandId`, `accessoryId`)
  - `GET /customer` (filters: `name`, `email`, `phone`)
  - `GET /repair-order` (filters: `status`, `customerId`, `deviceId`, `description`, `createdFrom`, `createdTo`)
- Pagination builder utility: `controller/support/PaginationUtils.java`.

### Add a new request field (e.g., "color" to Device)
1. **Entity** (`entity/Device.java`): Add `private String color;`
2. **Migration** (`db/migration/V8__*.sql`): `ALTER TABLE devices ADD COLUMN color VARCHAR(100);`
3. **Request DTO** (`controller/request/DeviceRequest.java`): Add `String color` field with `@NotNull` if required.
4. **Response DTO** (`controller/response/DeviceResponse.java`): Add `String color` field.
5. **Mapper** (`mapper/DeviceMapper.java`): MapStruct auto-maps; no code changes needed.
6. **Compile**: `./mvnw compile` (generates MapStruct implementation).

### Change JWT token claims
Edit **both** methods together:
- `config/TokenService.generateToken(JWTUserData)` — adds claims
- `config/TokenService.verifyToken(String)` — extracts same claims
- Ensure `config/SecurityFilter.java` still extracts `JWTUserData` correctly.

---

## 🎯 Where to Look First

| Need | Key Files |
|------|-----------|
| Understand architecture | `.agents/ARCHITECTURE.md` |
| Learn domain terms | `.agents/GLOSSARY.md` |
| Plan a feature | `.agents/PLANS.md` |
| Add new endpoint | `controller/*.java`, `service/*.java`, `repository/*.java` |
| Change listing filters/pagination | `controller/support/PaginationUtils.java`, `repository/specification/*.java`, `service/*Service.java` |
| Change security | `config/SecurityConfig.java`, `config/SecurityFilter.java`, `config/TokenService.java` |
| Handle errors | `config/ApplicationControllerAdvice.java` |
| View data model | `entity/*.java`, `db/migration/*` |
| Map DTOs | `mapper/*.java` (MapStruct interfaces) |

---

## ⚠️ Known Gotchas

- **Docker Compose**: Only spins up DB, not the app. Run app locally via `./mvnw spring-boot:run`.
- **JWT Secret**: Must be set in `application.properties` or environment; used in CI/CD.
- **MapStruct Timing**: Implementations generated only at compile time; always run `./mvnw compile` after changing mappers.
- **Migrations**: Append-only; never edit old `V*.sql` files; create new ones for schema changes.
- **Optional Returns**: Services use `Optional<Entity>` for single lookups; controllers must check `isPresent()`.
- **Listing Scope**: Keep `Brand` and `Accessory` as simple lists unless product requirements change (low-volume lookup entities).

---

## 📋 Documentation Maintenance Policy

- Whenever you make a change that is **crucial to the application**, update the agent guidance files so they stay accurate.
- Treat the following as crucial changes: architecture or package-structure changes, security/auth flow changes, database schema or migration changes, core API contract changes, new integrations/dependencies, and any change that alters established conventions.
- Update `AGENTS.md` first, and also keep the related `.agents/*` guidance files in sync when they exist or are introduced later.
- If a change affects how future agents should navigate, build, test, or safely modify the project, document that impact in the agent files in the same PR/commit.

---

## 🔗 Next Steps

1. **Read [.agents/GLOSSARY.md](.agents/GLOSSARY.md)** to understand the domain.
2. **Study [.agents/ARCHITECTURE.md](.agents/ARCHITECTURE.md)** to know where code belongs.
3. **Reference [.agents/PLANS.md](.agents/PLANS.md)** when building complex features.
4. **Run locally**: `docker compose up -d` (DB only), then `./mvnw spring-boot:run` (app).
5. **Test an endpoint** via Swagger UI or `curl`.

---

**Last updated**: 2026-07-20  
**Version**: 2.2 (added documentation maintenance policy for impactful changes)


