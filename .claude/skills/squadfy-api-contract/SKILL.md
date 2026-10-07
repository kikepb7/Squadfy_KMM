---
name: squadfy-api-contract
description: |
  How the Squadfy KMP app consumes the backend REST API v1 (Squadfy_Backend, Spring Boot): where the contract lives (BACKEND.md, OpenAPI), base URL /api/v1, error format {code,message}, 201/204, DTO resilience, fixtures, and how to request backend changes via its SDD. Use this skill whenever adding or changing a repository/data source, DTO, mapper, endpoint call, error mapping or enum, when the app gets 404/400/403/409/SERIALIZATION errors from the backend, or when checking client/backend alignment. Trigger on phrases like "endpoint", "DTO", "API", "backend returns", "404", "SERIALIZATION", "error code", "api/v1", "OpenAPI", "swagger", "migrate to v1", "align with backend".
---

# Consuming the backend API v1

## Where the contract lives (in priority order)
1. **OpenAPI**, the executable contract. Run the backend in `dev` and open `http://localhost:8080/v3/api-docs` (Swagger UI at `/swagger-ui.html`).
2. `../Squadfy_Backend/docs/BACKEND.md`:
   - §5 conventions
   - §6 auth
   - §8 endpoints with permissions
   - §9 DTOs
   - §10 enums
   - §11 errors
   - §12 WebSocket
   - §13 push
3. `../Squadfy_Backend/docs/api/migracion-v1.md`, the old → new route table.
4. `specs/contracts/api-v1.md` (this repo): which app repository or spec consumes each endpoint, plus the DTO deltas.

**Never claim an endpoint exists from memory.** Verify it:
```bash
grep -rn "@\(Get\|Post\|Put\|Patch\|Delete\)Mapping\|@RequestMapping" ../Squadfy_Backend --include=*.kt | grep -v /build/
git -C ../Squadfy_Backend branch --show-current && git -C ../Squadfy_Backend log --oneline -3
```
Note: `backend-documentation` is ahead of `master` (BE-GAP-7). Check which branch is deployed or running.

## Client rules
- **Base URL** ends in `/api/v1` (`BASE_URL_HTTP`). Routes in code are relative and never contain `/api`.
- **Json:** `ignoreUnknownKeys = true`, `coerceInputValues = true`, `explicitNulls = false`.
- **DTOs:** every optional or new field has a **default value**. Nullable without a default is still required and fails with SERIALIZATION. Receive enums as `String` and map them to domain enums with a fallback (APP-RN-13). The backend sends nulls explicitly; it does not omit them.
- **Ids** are UUID strings. Distinguish `userId` from `clubMemberId`.
- **Instants** are ISO-8601 UTC with microseconds; parse them with `kotlin.time.Instant`. `LocalTime` is `"HH:mm:ss"`. Display in the club `timeZone`.
- **Status codes:**
  - 201 on creations (clubs, schedule, matches, chats, devices, events);
  - 204 on leave, kick, ban and unban (map to `Unit`);
  - 401 **without a body** when the token is missing or invalid.
- **Errors:** parse `{code, message}` or `{code: "VALIDATION_ERROR", errors: [...]}` into `RemoteError(status, BackendErrorCode, messages)` (spec 002).
  - Branch the UI on **`code` + HTTP status, never on `message`** (it is English and only indicative).
  - `USER_EXITS` is the backend spelling of `USER_EXISTS`; accept both.
- **Auth:** the refresh token **rotates**, so persist the new pair after every refresh. A 401 `INVALID_TOKEN` on refresh means logout. A 429 on refresh is **not** a logout.
- **Realtime:** only chat uses WebSocket (`/ws/chat`, `Authorization` header on the handshake, JSON envelope `{type, payload: "<json string>"}`). Announcements and matches are refreshed on screen open, on pull-to-refresh, on push and after actions (ADR-0006).

## Wiring checklist (`feature/<f>/data`)
- [ ] The DTO mirrors `BACKEND.md` §9 or OpenAPI field by field, with defaults.
- [ ] A **fixture JSON** copied from `BACKEND.md` examples or a real dev response lives in `src/commonTest/resources/fixtures/`, and a mapper test decodes it (constitution V.4).
- [ ] A mapper converts the DTO to the domain model and maps enums safely.
- [ ] The repository uses the `core/data` helpers (`get/post/put/patch/delete`) and returns `Result<T, RemoteError>` or a feature error mapped from `code`.
- [ ] A `MockEngine` test covers success, the 201/204 cases, and at least one business error `code` (helper: `feature/chat/data/src/androidUnitTest/.../MockHttpClientFactory.kt`).
- [ ] The row in `specs/contracts/api-v1.md` changes from 🔁/➕ to ✅.

## Need something the backend does not have?
Do **not** work around it in the client (constitution I.1). Instead:
1. Add the gap to `specs/contracts/gap-analysis.md` §3.
2. Propose a spec in `../Squadfy_Backend/specs/NNN-*/` following that repo's SDD (`specs/README.md`, constitution, `_templates/`). Only do this if the user puts the backend in scope; otherwise draft it in the app spec's "Preguntas abiertas".
3. Mark the app spec as dependent on it in `specs/roadmap.md`.

## Running the backend locally (for E2E)
```bash
cd ../Squadfy_Backend && docker compose up -d
JWT_SECRET_BASE64=$(openssl rand -base64 32) FIREBASE_ENABLED=false ./gradlew :app:bootRun --args='--spring.profiles.active=dev'
# Android emulator → http://10.0.2.2:8080/api/v1 · Mailpit (emails) http://localhost:8025
```
