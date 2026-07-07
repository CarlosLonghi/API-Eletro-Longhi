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
- Services return Optional for single-entity lookups and raw Lists for collections (e.g. `DeviceService.findById` returns Optional; `findAll` returns List). Controllers rely on Optional to decide 404 vs 200.
- Repositories use Spring Data naming conventions for queries (example: `DeviceRepository.findDevicesByBrandId(Long brandId)`). Use the same naming style for derived queries.
- Validation and error handling: controllers use `@Valid` on records; global exception translation is in `config/ApplicationControllerAdvice` (maps MethodArgumentNotValidException → field errors map, DataIntegrityViolationException → 409). When adding new validations, ensure messages match existing mapping patterns.

Build / run / debug workflows (commands and gotchas)
- Build package: `./mvnw -q -DskipTests package` or full build `./mvnw package`.
- Run locally: `./mvnw spring-boot:run`. The app reads `src/main/resources/application.properties` for DB and JWT secret.
- Docker: repository includes `docker-compose.yml` (defines `db` service using postgres:16). Note: current compose exposes only the DB; the app isn't containerized here — run the app locally or add a Dockerfile to containerize it.
- Database: Flyway migrations are in `src/main/resources/db/migration` (V1..V7). Before running locally, create the DB (README shows `CREATE DATABASE eletro_longhi;`) or use docker compose to start Postgres.
- Tests: standard Maven tests `./mvnw test`. There are currently no extensive test suites in repo (README lists tests as future work).

Integration points and external dependencies
- PostgreSQL (jdbc URL configured in `application.properties`). Flyway applies SQL migrations from `src/main/resources/db/migration`.
- JWT uses auth0 Java JWT library; secret is read from `spring.security.secret` property — set this in `application.properties` or environment for CI.
- OpenAPI/Swagger is enabled via springdoc; docs available at `/swagger-ui/index.html` when app runs.

Quick examples an agent should follow
- Add a new endpoint that returns devices by brand: follow pattern in `controller/DeviceController.getDevicesByBrandId` → call `DeviceService.findDevicesByBrandId` → repository method `DeviceRepository.findDevicesByBrandId`.
- To add a new request field: update the request record in `controller/request`, update the MapStruct mapper (e.g. `DeviceMapper.toEntity`) and service logic; add `@NotNull/@NotBlank` annotations as needed to reuse `ApplicationControllerAdvice` validation mapping. Remember MapStruct mappers are Spring beans — update call-sites to inject the mapper and call `mapper.toEntity(...)` / `mapper.toResponse(...)`.
- MapStruct specifics to keep in mind:
  - MapStruct generates implementations at compile time; ensure `mapstruct-processor` is configured in `pom.xml` (it is in this project) and run a Maven build to create implementations.
  - If mapping collection of IDs to entities (List<Long> → List<Accessory>) maintain the helper methods (present in `DeviceMapper`) or add element mapping methods so MapStruct can generate code.
  - To suppress "unmapped target properties" warnings for generated fields (id, createdAt, updatedAt) you can add `unmappedTargetPolicy = ReportingPolicy.IGNORE` to `@Mapper` (requires importing `org.mapstruct.ReportingPolicy`).
- To change auth token claims: edit `config/TokenService.generateToken` and `verifyToken` together (both use the same claim keys `userId`, `userName`). Ensure `config/SecurityFilter` still extracts JWTUserData.

Where to look first (key files)
- Entry point: `EletrolonghiApplication.java`
- Security: `config/SecurityConfig.java`, `config/SecurityFilter.java`, `config/TokenService.java`
- Global errors: `config/ApplicationControllerAdvice.java`
- Example controller/service/repository: `controller/DeviceController.java`, `service/DeviceService.java`, `repository/DeviceRepository.java`
- DTOs & mappers: `controller/request/*.java`, `controller/response/*.java`, `mapper/*.java` — after the MapStruct migration, open `mapper/*.java` first to inspect `@Mapper` annotations, helper default methods (id→entity) and `componentModel` settings.
- DB migrations: `src/main/resources/db/migration/*` and `application.properties`

- Notes and discovered inconsistencies
- README claims `docker-compose.yml` orchestrates `app` + `db`, but the checked-in `docker-compose.yml` currently only defines `db`. Agents should not assume the app is containerized by compose.
- Use the `spring.security.secret` property (present in `application.properties`) when generating/verifying tokens in any test or CI environment.
- Mappers were migrated from manual static utility classes to MapStruct interfaces. Key practical impacts:
  - Controllers/services now inject mapper beans (e.g. `private final DeviceMapper deviceMapper;`) and call `deviceMapper.toEntity(...)` / `deviceMapper.toResponse(...)`.
  - MapStruct will generate implementations only at compile time; ensure `./mvnw compile` or `./mvnw package` is run during development/CI.
  - Some mappers include helper default methods for id→entity conversion and explicit list mapping to preserve prior behavior (see `DeviceMapper.map(List<Long>)`).

If you need to run quick checks
- Start DB only: `docker compose up -d --build` (spins up postgres as defined).
- Start app locally: `./mvnw spring-boot:run` (ensure DB credentials in `application.properties` match the running DB).

End of file.

