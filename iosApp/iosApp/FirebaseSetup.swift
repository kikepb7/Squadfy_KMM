import ComposeApp
import FirebaseAppCheck
import FirebaseCore

/// Firebase starts before the shared Kotlin code (spec 019): `FirebaseInitializer` wires the emulators, crash reporting
/// and Remote Config right after Koin, so Firebase must already be configured by then.
enum FirebaseSetup {

    static func configure() {
        // AC-019-20: App Check has to be installed before FirebaseApp.configure()
        if AppCheck_iosKt.shouldInstallAppCheck() {
            AppCheck.setAppCheckProviderFactory(SquadfyAppCheckProviderFactory())
        }
        FirebaseApp.configure()
    }
}

/// Debug builds use the debug provider (register the logged token in Firebase console > App Check); release builds
/// use App Attest, which needs the App Attest entitlement and a real device.
private final class SquadfyAppCheckProviderFactory: NSObject, AppCheckProviderFactory {

    func createProvider(with app: FirebaseApp) -> AppCheckProvider? {
        #if DEBUG
        return AppCheckDebugProvider(app: app)
        #else
        return AppAttestProvider(app: app)
        #endif
    }
}
