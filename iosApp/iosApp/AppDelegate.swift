import Foundation
import ComposeApp
import UIKit
import UserNotifications
import FirebaseCore
import FirebaseMessaging

class AppDelegate: NSObject, UIApplicationDelegate, UNUserNotificationCenterDelegate, MessagingDelegate {

    /// Google and Apple sign-in sheets for the shared Kotlin code (spec 019 AC-019-23)
    private let socialSignIn = SocialSignInHandler()
    
    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
        // Firebase is configured in iOSApp.init(), before Koin (FirebaseSetup)
        IosSocialSignIn.shared.bridge = socialSignIn
        // Spec 021 AC-021-07: app-wide notices by FCM topic, subscribed from the shared code once there is a token
        IosPushTopics.shared.bridge = self
        
        UNUserNotificationCenter.current().delegate = self
        Messaging.messaging().delegate = self
        
        return true
    }
    
    func application(_ application: UIApplication, didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data) {
        Messaging.messaging().apnsToken = deviceToken
        
        refreshToken()
    }
    
    func application(_ application: UIApplication, didFailToRegisterForRemoteNotificationsWithError error: Error) {
        #if DEBUG
        print("iOS: Failed to register for push notifications: \(error.localizedDescription)")
        #endif
    }
    
    func messaging(_ messaging: Messaging, didReceiveRegistrationToken fcmToken: String?) {
        guard let token = fcmToken, !token.isEmpty else {
            refreshToken()
            return
        }
        
        UserDefaults.standard.set(token, forKey: "FCM_TOKEN")
        IosDeviceTokenHolderBridge.shared.updateToken(token: token)
    }
    
    func application(_ application: UIApplication, didReceiveRemoteNotification userInfo: [AnyHashable : Any], fetchCompletionHandler completionHandler: @escaping (UIBackgroundFetchResult) -> Void) {
        Messaging.messaging().appDidReceiveMessage(userInfo)
        completionHandler(.newData)
    }
    
    // Spec 009 (AC-009-04): a push of the club on screen refreshes it in-app instead of showing a banner
    func userNotificationCenter(_ center: UNUserNotificationCenter, willPresent notification: UNNotification, withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void) {
        let content = notification.request.content
        if IosPushBridge.shared.shouldPresentInForeground(userInfo: content.userInfo, title: content.title, body: content.body) {
            completionHandler([.banner, .list, .sound])
        } else {
            completionHandler([])
        }
    }
    
    // Spec 009 (AC-009-03/06): every push type is routed like on Android
    func userNotificationCenter(_ center: UNUserNotificationCenter, didReceive response: UNNotificationResponse, withCompletionHandler completionHandler: @escaping () -> Void) {
        IosPushBridge.shared.onNotificationTapped(userInfo: response.notification.request.content.userInfo)
        completionHandler()
    }
    
    func refreshToken() {
        Task {
            do {
                let fcmToken = try await Messaging.messaging().token()
                
                UserDefaults.standard.set(fcmToken, forKey: "FCM_TOKEN")
                IosDeviceTokenHolderBridge.shared.updateToken(token: fcmToken)
            } catch {
                #if DEBUG
                print("iOS: Error getting FCM token: \(error.localizedDescription)")
                #endif
            }
        }
    }
}

/// Spec 021: FCM topics need the Firebase Messaging SDK, so the shared code asks Swift (PushTopicBridge).
extension AppDelegate: PushTopicBridge {
    func subscribe(topic: String, onDone: @escaping (KotlinBoolean) -> Void) {
        Messaging.messaging().subscribe(toTopic: topic) { error in
            onDone(KotlinBoolean(value: error == nil))
        }
    }

    func unsubscribe(topic: String, onDone: @escaping (KotlinBoolean) -> Void) {
        Messaging.messaging().unsubscribe(fromTopic: topic) { error in
            onDone(KotlinBoolean(value: error == nil))
        }
    }
}
