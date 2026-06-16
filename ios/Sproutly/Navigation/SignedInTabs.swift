import SwiftUI

enum AppTab: Hashable {
    case home, products, nearby, recipes
}

struct SignedInTabs: View {
    @EnvironmentObject var auth: AuthViewModel
    @State private var selected: AppTab = .home

    // Cross-tab hints emitted by deep-link-style nav (Products → Nearby).
    @State private var productStoreHint: String? = nil
    @State private var initialSupermarketMode: Bool = false

    var body: some View {
        TabView(selection: $selected) {
            NavigationStack { HomeScreen(onSwitchTab: switchTab) }
                .tabItem { Label("Home", systemImage: "house") }
                .tag(AppTab.home)

            NavigationStack {
                ProductsScreen(onOpenNearbyStore: { hint in
                    productStoreHint = hint
                    initialSupermarketMode = true
                    selected = .nearby
                })
            }
            .tabItem { Label("Products", systemImage: "bag") }
            .tag(AppTab.products)

            NavigationStack {
                NearbyScreen(
                    initialSupermarketMode: initialSupermarketMode,
                    productStoreHint: productStoreHint
                )
                .onDisappear {
                    // Reset deep-link hints after a visit so the next tab tap is clean.
                    productStoreHint = nil
                    initialSupermarketMode = false
                }
            }
            .tabItem { Label("Nearby", systemImage: "mappin.and.ellipse") }
            .tag(AppTab.nearby)

            NavigationStack { RecipesScreen() }
                .tabItem { Label("Recipes", systemImage: "fork.knife") }
                .tag(AppTab.recipes)
        }
        .tint(Palette.leafMint)
    }

    private func switchTab(_ tab: AppTab) {
        selected = tab
    }
}
