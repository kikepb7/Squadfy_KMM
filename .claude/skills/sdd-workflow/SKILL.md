---
name: sdd-workflow
description: |
  Spec-Driven Development workflow for Squadfy (specify → plan → tasks → implement → verify) using the specs/ folder. Use this skill whenever starting, continuing or reviewing any functional change: creating a new feature spec, writing a plan or task list, picking the next task, implementing a task, or closing a feature. Trigger on phrases like "new feature", "write a spec", "spec this", "plan the feature", "break into tasks", "next task", "implement T-0xx", "continue feature 00x", "SDD", "acceptance criteria", "roadmap", or any request to build MVP functionality (signup, draw, schedule, results, standings, roles, release).
---

# Spec-Driven Development in Squadfy

The source of truth lives in `specs/`. Read `specs/README.md` once per session for the full map. **No functional code is written without an `Approved` spec.**

## 0. Orient (always first)
1. Read `specs/roadmap.md` to see the feature status, the dependencies and the **open decisions (D-x)**.
2. Read `specs/constitution.md` (short). It is non-negotiable.
3. For a domain question, load the `squadfy-domain` skill. For anything that touches HTTP, load `squadfy-api-contract`.

## 1. Specify → `specs/features/NNN-slug/spec.md`
- Copy `specs/templates/spec.md`. NNN is the next free number. The slug is kebab-case.
- Write the **what and why** only. There is no tech design here.
- Every acceptance criterion:
  - has an ID `AC-NNN-xx`;
  - uses Given/When/Then;
  - is testable;
  - references the business rules (`BR-xxx`) it implements.
- Reference the rules you implement: backend domain rules as `BE-NNN RN-x` (Part A of `specs/product/business-rules.md`), client rules as `APP-RN-xx` (Part B).
- **Domain rules come from the backend** (ADR-0005). If the feature needs a domain rule or endpoint the backend lacks, do not invent it in the client. Record it in `specs/contracts/gap-analysis.md` §3 and propose a backend spec (see `squadfy-api-contract`). New **client-only** rules (UX, presentation) get a new `APP-RN` id, marked ❓ until the product owner confirms them.
- Put unknowns under "Preguntas abiertas" and add them to the decisions table in `roadmap.md`. **Do not guess silently.** If an answer blocks the work, ask the user.
- Status `Draft`. Only the user moves it to `Approved`.

## 2. Plan → `plan.md`
- Copy `specs/templates/plan.md` and fill in the constitution compliance table. Any ⚠️ needs a justification or an ADR (`specs/adr/NNNN-*.md` from `templates/adr.md`).
- Design layer by layer, following the existing project skills:
  - domain → `android-error-handling`
  - data → `android-data-layer`
  - DI → `android-di-koin`
  - presentation → `android-presentation-mvi` and `android-compose-ui`
  - navigation → `android-navigation`
  - modules → `android-module-structure`
- Room schema change → write an explicit `Migration(n, n+1)` plus a test. Never use destructive fallback.
- API change → edit `specs/contracts/api-v1.md` and its change log in the same PR, and list the backend tasks.
- Add a test-strategy table that maps **every AC to a test**.

## 3. Tasks → `tasks.md`
- Copy `specs/templates/tasks.md`.
- Each task is atomic (≤ ½ day), keeps the build green, and states how it is verified (`Verificación:`).
- Order the tasks domain → data → presentation → backend `(B)` → verification. Mark parallelizable tasks with `(P)`.

## 4. Implement (one task at a time)
1. Mark the task `[~]` in `tasks.md`.
2. Write the test first when the task carries logic. Name the tests after the AC they cover:
   ```kotlin
   @Test fun `AC-003-04 window is OPEN exactly at opensAt`() = runTest { … }
   ```
3. Implement the minimum needed to pass. Match the surrounding code style.
4. Verify:
   ```bash
   ./gradlew :feature:<f>:<layer>:testDebugUnitTest      # fast loop
   ./gradlew testDebugUnitTest                            # before marking done
   ./gradlew :composeApp:assembleDebug                    # if presentation / DI changed
   ```
5. Mark the task `[x]`.
6. If the user asks for a commit, use the message format `CLUB | 003 · T-002 SignupWindowPolicy state + tests`.
7. **Scope drift**: if the code needs behavior the spec does not describe, stop, update the spec or raise a question, then continue.

## 5. Verify and close
- Tick the spec's "Checklist de verificación" and confirm that every AC has a passing test that cites it:
  ```bash
  grep -rn "AC-003-" --include=*Test.kt .
  ```
- Run `/code-review` on the diff.
- Ask the user for a manual demo (Android emulator; iOS if there is UI).
- Update `specs/roadmap.md` (phase and counts).

## Guardrails
- Server-authoritative rules (window, capacity, draw, stats) are **validated in the backend**. Client logic is UX only (constitution I).
- Never hardcode user-visible strings. Never use magic `String` statuses outside the data layer.
- Builds use the JDK 17 that `gradle/gradle-daemon-jvm.properties` pins. If KSP complains about the Java version, check that file before anything else.
- Squadfy_Backend is a sibling repo (`../Squadfy_Backend`) with its **own SDD** (`specs/README.md`, `constitution.md`, numbered `BE-NNN` here). Change it only when the user includes it in scope, and then follow *its* SDD and `./gradlew build`. Otherwise list the work as a proposed backend spec in `specs/roadmap.md`.
- Unfinished or backend-pending work ships **behind a feature flag** (spec 013, ADR-0007) instead of being deleted or left visible. Add the flag in `FeatureFlag` with PRE and PRO defaults when you start the feature, and only flip PRO when the spec is `Done`. Guests, schedule exceptions, `drawTime` and manual score are kept behind flags until the backend ships them (D-1).
