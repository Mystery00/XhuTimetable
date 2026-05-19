import SwiftUI
import ComposeApp

@main
struct iOSApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var appDelegate

    init() {
        HelperKt.callAppInit()
    }
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
