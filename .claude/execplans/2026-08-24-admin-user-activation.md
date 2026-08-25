# ExecPlan: Admin account activation & user management

**Date**: 2026-08-24
**Status**: Complete

## Goal

A self-registered user cannot use the app until an ADMIN activates the account and assigns its role. ADMINs can list all users, change a user's role (role-only PATCH), and suspend/reactivate a user's access (status-only PATCH).

## Big Picture & Context

**Current state (before)**: `POST /auth/register` created an immediately-usable account — `User.isEnabled()` was hardcoded `true`, and there was no endpoint to list or manage users at all (CLAUDE.md literally said "promote to ADMIN manually in the DB").

**Desired state (after)**: New registrations start `enabled=false`. An ADMIN activates via `PATCH /user/{id}/status` (`enabled:true`) and sets the role via `PATCH /user/{id}/role`. ADMINs can also list users (`GET /user`, paginated/filterable) and suspend an active user (same status endpoint, `enabled:false`). This is the backend for a future admin "users table" UI (frontend is out of scope of this repo).

**Why now**: user request — see conversation. Anyone who registers today gets full access immediately with no gate, which isn't the desired trust model going forward.

**Affected modules**: `entity`, `service`, `controller` (+ `api/spec`, `request`, `response`), `repository` (+ `specification`), `config`, `exception`, a new Flyway migration, and tests across all of the above.

## Progress Checklist

- [x] Migration `V15__add_enabled_to_users.sql`
- [x] `entity/User.java` — `enabled` field wired to `isEnabled()`
- [x] `service/UserService.java` — `save` forces `enabled=false`; new `updateRole`/`updateStatus`/`findAll`
- [x] `exception/AccountNotActivatedException.java` + `ApplicationControllerAdvice` handler (403)
- [x] `controller/AuthController.java` — catch `DisabledException` on login
- [x] `controller/api/spec/AuthApi.java` — document 403 on login
- [x] `service/RefreshTokenService.java` — `@Transactional(readOnly = true)` + `enabled` check on refresh
- [x] `controller/request/UserRoleUpdateRequest.java`, `UserStatusUpdateRequest.java`
- [x] `controller/response/UserResponse.java` — add `enabled`
- [x] `repository/UserRepository.java` — `JpaSpecificationExecutor<User>`
- [x] `repository/specification/UserSpecification.java`
- [x] `controller/UserController.java` + `controller/api/spec/UserApi.java`
- [x] `config/SecurityConfig.java` — ADMIN-only matchers for `/user` endpoints
- [x] `config/AdminUserSeeder.java` — force `enabled=true` on seed admin
- [x] Docs: `CLAUDE.md`, `.claude/GLOSSARY.md`, `.claude/ARCHITECTURE.md`
- [x] Tests: extended `AuthorizationIntegrationTest`, `UserServiceTest`, `RefreshTokenServiceTest`, `AuthControllerTest`, `ApplicationControllerAdviceTest`; new `UserControllerTest`
- [x] `./mvnw test` green (139 tests, 0 failures)
- [x] `./mvnw verify` green (JaCoCo gate passes)

## Surprises & Discoveries

**2026-08-24**: `RefreshToken.user` is `@ManyToOne(fetch = FetchType.LAZY)` and `RefreshTokenService.findValidToken` was not `@Transactional` — calling `refreshToken.getUser().isEnabled()` would have risked a `LazyInitializationException` outside a session. Added `@Transactional(readOnly = true)` to the method alongside the new check.

**2026-08-24**: `AuthController.login` only caught `BadCredentialsException`; Spring's `DaoAuthenticationProvider` throws `DisabledException` (not a subtype) for a disabled `UserDetails`, which would have propagated as an unhandled 500 with no fix. Added a dedicated `catch (DisabledException)` clause.

**2026-08-24**: `UserMapper.toEntity(UserRequest)` builds via `new User()` (MapStruct, no-arg constructor) — confirmed the `Role role = Role.USER` and new `boolean enabled = true` field initializers apply there too (not just via Lombok's `@Builder`), so no mapper change was needed for either default.

## Decision Log

| Date | Decision | Rationale |
|------|----------|-----------|
| 2026-08-24 | Suspension is not instant on already-issued access tokens | Keeps `SecurityFilter` stateless (JWT-claims-only, no per-request DB hit) — user's explicit choice over adding a DB check per request. Refresh and new logins are still cut off immediately via `RefreshTokenService`/`AuthService`. |
| 2026-08-24 | Single `PATCH /user/{id}/status` (boolean `enabled`) reused for both activate and suspend, instead of separate `/activate`/`/suspend` endpoints | User's explicit choice; matches the existing single-field-PATCH convention (`RepairOrder` status). |
| 2026-08-24 | Migration uses `DEFAULT TRUE`; `enabled=false` is forced in `UserService.save`, not the DB default | Avoids retroactively locking out any pre-existing user (including the seed admin) when `V15` runs. |
| 2026-08-24 | `loginAttemptService.loginFailed(email)` is called on a disabled-account login attempt, same as any other failed login | Keeps throttling behavior uniform; not explicitly dictated by the user, a judgment call made during implementation. |

## Context & Orientation

**Assumptions**: Frontend (an admin "users table" UI) is out of scope — this repo is backend-only. `Role` stays `ADMIN`/`USER` for now; the design (plain `Role` enum, `PATCH /user/{id}/role`) already accommodates future roles without further changes.

**Key files**: see Progress Checklist above for the full file list.

**Related docs**: `.claude/GLOSSARY.md` ("Account activation & suspension"), `.claude/ARCHITECTURE.md` (invariant I9).

## Validation & Acceptance

**Acceptance criteria**:
- [x] `POST /auth/register` creates a user with `enabled=false`.
- [x] `POST /auth/login` for a disabled user returns 403.
- [x] ADMIN can list users, change role, and change status; USER gets 403 on all three.
- [x] Suspending a user blocks new login (403) and refresh (401); role/status PATCHes only touch their own field.
- [x] `./mvnw verify` passes (tests + JaCoCo 0.80 gate).

**Test scenarios** — see `AuthorizationIntegrationTest` (`shouldForbidUserFromListingUsers`, `shouldAllowAdminToListUsers`, `shouldForbidUserFromUpdatingUserRole`, `shouldAllowAdminToUpdateUserRole`, `shouldForbidUserFromUpdatingUserStatus`, `shouldAllowAdminToSuspendUser`, `shouldRejectLoginForDisabledUser`, `shouldRejectRefreshForDisabledUser`), `UserServiceTest`, `UserControllerTest`, `RefreshTokenServiceTest.shouldFailForDisabledUser`, `AuthControllerTest.shouldFailLoginWithDisabledAccount`.

## Outcomes & Retrospective

**What shipped**: Full activation/suspension/role-management flow described in Goal, migration V15, all listed files, and the test suite above. `./mvnw test` (139 tests) and `./mvnw verify` (JaCoCo gate) both green.

**What we learned**: The two "surprises" above (lazy refresh-token user + uncaught `DisabledException`) were both latent gaps in the existing auth code that this feature's design forced into the open — worth remembering that `RefreshTokenService` methods generally need to be transactional whenever they touch `RefreshToken.user`.

**What's left**: Frontend admin table (explicitly out of scope here). No further backend work identified.
