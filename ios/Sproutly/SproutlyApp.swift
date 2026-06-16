import SwiftUI

@main
struct SproutlyApp: App {
    @StateObject private var auth = AuthViewModel()

    init() {
        // Force-dark like the Android Material 3 dark scheme.
        UITabBar.appearance().barTintColor = UIColor(Palette.bgSurface)
        UITabBar.appearance().backgroundColor = UIColor(Palette.bgSurface)
    }

    var body: some Scene {
        WindowGroup {
            RootView()
                .environmentObject(auth)
                .preferredColorScheme(.dark)
                .tint(Palette.leafMint)
        }
    }
}

struct RootView: View {
    @EnvironmentObject var auth: AuthViewModel

    var body: some View {
        ZStack {
            Palette.bgDeep.ignoresSafeArea()
            switch auth.state {
            case .loading:
                ProgressView()
                    .tint(Palette.leafMint)
            case .signedOut:
                LoginScreen()
            case .signedIn:
                SignedInTabs()
            }
        }
    }
}
