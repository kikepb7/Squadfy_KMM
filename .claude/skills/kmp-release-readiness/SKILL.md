---
name: kmp-release-readiness
description: |
  Release and production-readiness checklist for the Squadfy KMP app (Android Play + iOS TestFlight) and its CI: signing, R8/ProGuard rules, versioning, per-build-type URLs, cleartext, network logging, token encryption, backups, push setup, i18n, secrets, GitHub Actions. Use this skill whenever preparing a release build, touching build-logic release config, the CI workflow, the AndroidManifest, the iOS project config, or when asked "is it ready to deploy", "prepare release", "sign the app", "proguard", "minify", "CI is failing", "secrets", "versionCode".
---

# Release readiness (Squadfy KMP)

Tracked as feature `specs/features/011-release-readiness` (app) and `012-go-live` (end to end with the backend and the stores). Tick their ACs as items land.

## Build environment
- Gradle uses the JDK 17 that `gradle/gradle-daemon-jvm.properties` pins. Machines with an EA JDK (e.g. `23-valhalla`) break KSP if the pin is removed.
- KSP must match Kotlin: `ksp = "<kotlinVersion>-2.0.x"` in `gradle/libs.versions.toml`.
- Secrets come from `-P` properties, then env vars, then `local.properties`. See `BuildKonfigConventionPlugin`.
  - There is **no client API key** (ADR-0008). Never add one back: anything compiled into the app can be extracted. Users authenticate with JWT; client attestation (Play Integrity / App Attest) is the option if it is ever needed.
  - `BASE_URL_HTTP` (must end in `/api/v1`) and `BASE_URL_WS` (`…/ws`) default to the Android emulator (`10.0.2.2`). **That default is debug-only.**
  - `composeApp/google-services.json` is gitignored. CI writes it from the `GOOGLE_SERVICES_JSON` secret.

## Android release checklist
- [ ] `signingConfigs.release` reads from `keystore.properties` or env (`SIGNING_STORE_FILE`, `SIGNING_STORE_PASSWORD`, `SIGNING_KEY_ALIAS`, `SIGNING_KEY_PASSWORD`). Never commit `*.jks`.
- [ ] `isMinifyEnabled = true` and `isShrinkResources = true` in `AndroidApplicationConventionPlugin`, with `proguard-rules.pro` keeping:
  - kotlinx-serialization: `@Serializable` classes and their `Companion`/`$serializer`
  - Ktor
  - Room entities and DAOs
  - Koin reflection-free (usually nothing needed)
  - Firebase messaging service
- [ ] `versionCode` from `-PversionCode` (CI: `${{ github.run_number }}`); `versionName` follows SemVer.
- [ ] Release `BASE_URL_*` are `https://` and `wss://`. Fail the build if a release variant resolves to `http`.
- [ ] Remove `android:usesCleartextTraffic="true"`. Allow cleartext in `network_security_config` only from the debug source set.
- [ ] Ktor `Logging`: `LogLevel.NONE` in release; in debug, `HEADERS` with `sanitizeHeader { it == HttpHeaders.Authorization }`. Koin `androidLogger()` only in debug. Do not log FCM tokens.
- [ ] Tokens encrypted at rest (Keystore-backed). Set `android:allowBackup="false"`, or add `dataExtractionRules` that exclude the session DataStore.
- [ ] App label "Squadfy", adaptive icon, splash.
- [ ] Push: `FirebaseMessagingService.onMessageReceived` and a notification channel created at startup.
- [ ] Hide unfinished modules (`economy`, `onboarding`) and mock sections (Home matches/news).
- [ ] Smoke test the **minified release** on a device: login, create/join club, sign up, draw, result.

## Store requirements (blocking)
- [ ] **In-app account deletion**: Apple Guideline 5.1.1(v), and Google Play also requires a **web** deletion URL. This needs a backend endpoint (proposed BE-008, BE-GAP-1).
- [ ] Privacy policy URL, linked from Register and Profile. Fill in Play **Data Safety** and the App Store **privacy labels**: email, username, photo, device token, chat content, crash data if used.
- [ ] Play: new personal developer accounts must run a closed test with **12 testers for 14 days** before production access.
- [ ] App Review: provide a demo account with a club, an open announcement and teams.
- [ ] Backend `/auth/refresh` rate limit must not log out active users (proposed BE-009, BE-GAP-2).

## iOS checklist
- [ ] `iosApp/Configuration/Config.xcconfig`: set `TEAM_ID`. One canonical bundle ID across platforms.
- [ ] `aps-environment = production` for release; push capability; APNs key uploaded to Firebase.
- [ ] Associated Domains (`applinks:squadfy.<domain>`) plus `apple-app-site-association` on the server.
- [ ] No ATS exceptions: the backend must be HTTPS.
- [ ] Stop tracking `GoogleService-Info.plist`; restrict the iOS API key by bundle ID.
- [ ] Remove `print()` from `AppDelegate.swift`.

## CI (`.github/workflows/squadfy-ci.yml`)
- Runs on push and PR to `main` (unit tests for all modules, assembleDebug, ktlint report-only).
- CI needs no secrets: without `GOOGLE_SERVICES_JSON` it uses `.github/ci/google-services.placeholder.json`.
- Release secrets (signing, Play service account, PRO URLs, production `google-services.json`) live in the protected `production` Environment, which needs approval from its required reviewers (`.github/workflows/squadfy-release.yml`).
- To add a release job: decode the keystore from a base64 secret, then run `./gradlew :composeApp:bundleRelease -PversionCode=${{ github.run_number }}` and upload the AAB.

## Verification commands
```bash
./gradlew testDebugUnitTest
./gradlew :composeApp:assembleRelease          # after the signing config exists
./gradlew :composeApp:lintRelease
unzip -p composeApp/build/outputs/apk/release/*.apk AndroidManifest.xml | strings | grep -i cleartext   # expect nothing
```
