import SwiftUI
import ComposeApp

@main
struct iOSApp: App {
    init() {
        HelperKt.callAppInit()
    }
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}