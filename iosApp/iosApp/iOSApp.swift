import SwiftUI
import ComposeApp

@main
struct iOSApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var delegate

    init() {
        // Before Koin: the shared code expects Firebase (and App Check) to be ready
        FirebaseSetup.configure()
        InitKoinKt.doInitKoin()
    }
    
    var body: some Scene {
        WindowGroup {
            ContentView()
                .onOpenURL { url in
                    // Google sign-in comes back through its URL scheme (spec 019)
                    if SocialSignInHandler.handle(url: url) { return }
                    ExternalUriHandler.shared.onNewUri(uri: url.absoluteString)
                }
        }
    }
}
