# Firebase config (iOS)

The Firebase project **Squadfy** (`squadfy-5233d`) has one iOS app per environment (spec 019 D-14). Download each
`GoogleService-Info.plist` from the Firebase console (or with
`firebase apps:sdkconfig IOS <appId> --project squadfy-5233d`) and drop it here. Both files are gitignored.

| Xcode configuration | Environment | App in Firebase | Bundle ID | File |
|---|---|---|---|---|
| Debug | PRE | Squadfy iOS Dev | `com.kikepb.squadfy.dev` | `Debug/GoogleService-Info.plist` |
| Release | PRO | Squadfy iOS Prod | `com.kikepb.squadfy` | `Release/GoogleService-Info.plist` |

The "Select Firebase config" build phase copies the right one into the app. Without it the build still runs, but
without Firebase (Xcode shows a warning). "Register Google sign-in URL scheme" reads its `REVERSED_CLIENT_ID`, and
"Upload Crashlytics dSYMs" runs on Release only.
