import SwiftUI

struct RecipesScreen: View {
    private let featured = Recipe(title: "Smoky chickpea bowl", subtitle: "25 min · high protein", tag: "Featured")
    private let quick = [
        Recipe(title: "15-min peanut tofu", subtitle: "15 min · weeknight", tag: "Quick"),
        Recipe(title: "Lemon herb couscous", subtitle: "12 min · pantry", tag: "Quick"),
        Recipe(title: "Crispy mushroom tacos", subtitle: "20 min · fun", tag: "Quick"),
    ]
    private let seasonal = [
        Recipe(title: "Roast squash soup", subtitle: "winter", tag: "Seasonal"),
        Recipe(title: "Strawberry oat parfait", subtitle: "summer", tag: "Seasonal"),
    ]

    var body: some View {
        ScreenBackground {
            ScrollView {
                VStack(alignment: .leading, spacing: 20) {
                    HStack {
                        Text("Recipes")
                            .font(SproutlyType.displaySmall)
                            .foregroundColor(Palette.textPrimary)
                        Spacer()
                        NavigationLink(destination: MealPlanScreen()) {
                            Label("Plan", systemImage: "calendar")
                                .font(SproutlyType.labelLarge)
                                .padding(.horizontal, 12).padding(.vertical, 6)
                                .background(Palette.leafMint)
                                .foregroundColor(Palette.bgDeep)
                                .clipShape(Capsule())
                        }
                    }
                    .padding(.top, 8)

                    SproutlyCard(accent: true) {
                        SectionLabel(text: featured.tag)
                        Text(featured.title).font(SproutlyType.titleLarge).foregroundColor(Palette.textPrimary)
                        Text(featured.subtitle).font(SproutlyType.bodyMedium).foregroundColor(Palette.textMuted)
                        MintPillButton(label: "Open recipe") {}
                    }

                    section(title: "Quick wins", items: quick)
                    section(title: "Seasonal picks", items: seasonal)
                }
                .padding(.horizontal, 20)
                .padding(.bottom, 24)
            }
        }
        .navigationBarTitleDisplayMode(.inline)
    }

    private func section(title: String, items: [Recipe]) -> some View {
        VStack(alignment: .leading, spacing: 10) {
            SectionLabel(text: title)
            ForEach(items) { r in
                SproutlyCard {
                    HStack {
                        VStack(alignment: .leading, spacing: 4) {
                            Text(r.title).font(SproutlyType.titleMedium).foregroundColor(Palette.textPrimary)
                            Text(r.subtitle).font(SproutlyType.bodyMedium).foregroundColor(Palette.textMuted)
                        }
                        Spacer()
                        Image(systemName: "chevron.right").foregroundColor(Palette.textMuted)
                    }
                }
            }
        }
    }
}

struct Recipe: Identifiable {
    let id = UUID()
    let title: String
    let subtitle: String
    let tag: String
}
