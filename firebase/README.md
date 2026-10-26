# Firebase (spec 019, ADR-0010)

Server side of the Firebase variant of Squadfy: the project is **Squadfy** (`squadfy-5233d`).

| File | What it is |
|---|---|
| `firebase.json` | Products, Functions codebase `squadfy` (Node 22, `europe-west1`), Hosting and the Emulator Suite ports |
| `firestore.rules`, `storage.rules`, `database.rules.json` | Security rules (members read; the domain is written by Functions) |
| `firestore.indexes.json` | Composite indexes |
| `remoteconfig.template.json` | Feature flags (spec 013 keys). `useInAppDefault` keeps the PRE/PRO default; add a conditional value per app to switch one remotely |
| `functions/` | Domain server in TypeScript: callables, scheduled jobs and triggers; tests with Vitest |
| `hosting/` | Privacy policy, account deletion page and the Auth action page |

## Local development (no real project involved)
```bash
cd firebase/functions && npm ci && npm run build
npm test                    # unit tests of the domain logic
npm run test:emulator       # rules and callables against the Emulator Suite
npm run serve               # emulators for the app, with the UI at http://localhost:4006
```
`serve` runs the emulators under the app's project id (`squadfy-5233d`), because the app calls them with the id of its
Firebase config: a different id answers NOT_FOUND. Everything stays local; the functions never send real pushes from
the emulator (`FUNCTIONS_EMULATOR`). The tests use the offline `demo-squadfy` project instead.
The app talks to the emulators with `squadfy.firebase.useEmulator=true` in `local.properties` (debug builds only).
The ports are not the usual defaults, so they never clash with TensoScan or the own backend on 8080.

## Deploy (only with the owner's go-ahead)
```bash
cp .firebaserc.example .firebaserc
firebase deploy --only firestore,database,storage,functions,remoteconfig,hosting
```
