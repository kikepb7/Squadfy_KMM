import AuthenticationServices
import CryptoKit
import FirebaseAuth
import FirebaseCore
import GoogleSignIn
import UIKit
import ComposeApp

/// Swift side of `SocialSignInBridge`: Sign in with Apple (AuthenticationServices) and Google (Google Sign-In SDK).
/// Only the provider sheets live here; signing in to Firebase and everything after it is shared Kotlin code.
final class SocialSignInHandler: NSObject, SocialSignInBridge {

    /// Kept alive while Apple's sheet is open: the controller only holds its delegate weakly.
    private var appleFlow: AppleSignInFlow?

    func signInWithGoogle(onResult: @escaping (SocialSignInOutcome) -> Void) {
        // Present only once Google is enabled in Firebase Auth and the plist was downloaded again with CLIENT_ID
        guard let clientID = FirebaseApp.app()?.options.clientID else {
            onResult(SocialSignInOutcome.companion.unavailable())
            return
        }
        guard let presenter = UIApplication.shared.topViewController else {
            onResult(SocialSignInOutcome.companion.failed())
            return
        }
        GIDSignIn.sharedInstance.configuration = GIDConfiguration(clientID: clientID)
        GIDSignIn.sharedInstance.signIn(withPresenting: presenter) { result, error in
            if let error = error as? GIDSignInError, error.code == .canceled {
                onResult(SocialSignInOutcome.companion.cancelled())
            } else if let idToken = result?.user.idToken?.tokenString {
                onResult(SocialSignInOutcome.companion.google(idToken: idToken))
            } else {
                print("Google sign-in failed: \(error?.localizedDescription ?? "no ID token")")
                onResult(SocialSignInOutcome.companion.failed())
            }
        }
    }

    func signInWithApple(onResult: @escaping (SocialSignInOutcome) -> Void) {
        let flow = AppleSignInFlow { [weak self] outcome in
            self?.appleFlow = nil
            onResult(outcome)
        }
        appleFlow = flow
        flow.start()
    }

    func revokeAppleToken(authorizationCode: String, onDone: @escaping (String?) -> Void) {
        Auth.auth().revokeToken(withAuthorizationCode: authorizationCode) { error in
            onDone(error?.localizedDescription)
        }
    }

    /// Google returns to the app through its URL scheme (the reversed client id).
    static func handle(url: URL) -> Bool {
        GIDSignIn.sharedInstance.handle(url)
    }
}

/// One Sign in with Apple request. Apple signs the identity token with the SHA-256 of a random nonce and Firebase
/// checks it against the raw nonce, so a token can't be replayed from another sign-in.
private final class AppleSignInFlow: NSObject, ASAuthorizationControllerDelegate, ASAuthorizationControllerPresentationContextProviding {

    private let completion: (SocialSignInOutcome) -> Void
    private let rawNonce = AppleSignInFlow.randomNonce()

    init(completion: @escaping (SocialSignInOutcome) -> Void) {
        self.completion = completion
    }

    func start() {
        let request = ASAuthorizationAppleIDProvider().createRequest()
        // The name only comes the first time the user authorises the app; Kotlin keeps it for the profile step
        request.requestedScopes = [.fullName, .email]
        request.nonce = AppleSignInFlow.sha256(rawNonce)
        let controller = ASAuthorizationController(authorizationRequests: [request])
        controller.delegate = self
        controller.presentationContextProvider = self
        controller.performRequests()
    }

    func authorizationController(controller: ASAuthorizationController, didCompleteWithAuthorization authorization: ASAuthorization) {
        guard
            let credential = authorization.credential as? ASAuthorizationAppleIDCredential,
            let tokenData = credential.identityToken,
            let idToken = String(data: tokenData, encoding: .utf8)
        else {
            completion(SocialSignInOutcome.companion.failed())
            return
        }
        let authorizationCode = credential.authorizationCode.flatMap { String(data: $0, encoding: .utf8) }
        completion(
            SocialSignInOutcome.companion.apple(
                idToken: idToken,
                rawNonce: rawNonce,
                authorizationCode: authorizationCode,
                givenName: credential.fullName?.givenName,
                familyName: credential.fullName?.familyName
            )
        )
    }

    func authorizationController(controller: ASAuthorizationController, didCompleteWithError error: Error) {
        if let error = error as? ASAuthorizationError, error.code == .canceled {
            completion(SocialSignInOutcome.companion.cancelled())
        } else if let error = error as? ASAuthorizationError, error.code == .unknown {
            // What Apple answers when the device has no Apple Account (the user closed "sign in in Settings")
            completion(SocialSignInOutcome.companion.unavailable())
        } else {
            print("Sign in with Apple failed: \(error.localizedDescription)")
            completion(SocialSignInOutcome.companion.failed())
        }
    }

    func presentationAnchor(for controller: ASAuthorizationController) -> ASPresentationAnchor {
        UIApplication.shared.keyWindow ?? ASPresentationAnchor()
    }

    private static func randomNonce(length: Int = 32) -> String {
        let charset = Array("0123456789ABCDEFGHIJKLMNOPQRSTUVXYZabcdefghijklmnopqrstuvwxyz-._")
        var bytes = [UInt8](repeating: 0, count: length)
        let status = SecRandomCopyBytes(kSecRandomDefault, length, &bytes)
        precondition(status == errSecSuccess, "Unable to generate a nonce: \(status)")
        return String(bytes.map { charset[Int($0) % charset.count] })
    }

    private static func sha256(_ input: String) -> String {
        SHA256.hash(data: Data(input.utf8)).map { String(format: "%02x", $0) }.joined()
    }
}

private extension UIApplication {
    var keyWindow: UIWindow? {
        connectedScenes
            .compactMap { $0 as? UIWindowScene }
            .flatMap(\.windows)
            .first { $0.isKeyWindow }
    }

    /// The controller on top, which presents Google's sheet.
    var topViewController: UIViewController? {
        var top = keyWindow?.rootViewController
        while let presented = top?.presentedViewController { top = presented }
        return top
    }
}
