import UIKit
import UserNotifications
import ComposeApp

final class AppDelegate: NSObject, UIApplicationDelegate, UNUserNotificationCenterDelegate {
    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {
        let appKey = Bundle.main.object(forInfoDictionaryKey: "JPUSH_APPKEY") as? String ?? ""
        let channel = Bundle.main.object(forInfoDictionaryKey: "JPUSH_CHANNEL") as? String ?? ""
        JPUSHService.setup(
            withOption: launchOptions,
            appKey: appKey,
            channel: channel,
            apsForProduction: true
        )
        UNUserNotificationCenter.current().delegate = self
        application.registerForRemoteNotifications()
        refreshRegistrationId()
        return true
    }

    func application(
        _ application: UIApplication,
        didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data
    ) {
        JPUSHService.registerDeviceToken(deviceToken)
        refreshRegistrationId()
    }

    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        willPresent notification: UNNotification,
        withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void
    ) {
        completionHandler([.banner, .sound, .badge])
    }

    private func refreshRegistrationId() {
        JPUSHService.registrationIDCompletionHandler { _, registrationId in
            guard let registrationId, !registrationId.isEmpty else {
                return
            }
            IosPushBridge.shared.updateRegistrationId(registrationId: registrationId)
        }
    }
}
