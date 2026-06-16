import SwiftUI

struct HomeScreen: View {
    @EnvironmentObject var auth: AuthViewModel
    let onSwitchTab: (AppTab) -> Void

    var body: some View {
        ScreenBackground {
            ScrollView {
                VStack(alignment: .leading, spacing: 20) {
                    header
                    plantHero
                    quickActions
                    todayHighlights
                    weeklyPreview
                }
                .padding(.horizontal, 20)
                .padding(.bottom, 24)
            }
        }
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .topBarTrailing) {
                NavigationLink {
                    AccountScreen()
                } label: {
                    Image(systemName: "person.circle")
                        .foregroundColor(Palette.leafMint)
                }
            }
        }
    }

    private var header: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(greeting)
                .font(SproutlyType.labelLarge)
                .foregroundColor(Palette.textMuted)
            Text(displayName)
                .font(SproutlyType.displaySmall)
                .foregroundColor(Palette.textPrimary)
        }
        .padding(.top, 8)
    }

    private var displayName: String {
        if case let .signedIn(user) = auth.state {
            return user.displayName.capitalized
        }
        return "Sprout"
    }

    private var greeting: String {
        let hour = Calendar.current.component(.hour, from: Date())
        switch hour {
        case 5..<12: return "Good morning"
        case 12..<18: return "Good afternoon"
        default: return "Good evening"
        }
    }

    private var plantHero: some View {
        SproutlyCard(accent: true) {
            HStack(alignment: .top, spacing: 16) {
                PlantHeroCanvas()
                    .frame(width: 96, height: 96)
                VStack(alignment: .leading, spacing: 6) {
                    SectionLabel(text: "Today")
                    Text("Eat one more plant-rich meal")
                        .font(SproutlyType.titleLarge)
                        .foregroundColor(Palette.textPrimary)
                    Text("Tap a recipe to lock it in, or generate a meal plan to map out the week.")
                        .font(SproutlyType.bodyMedium)
                        .foregroundColor(Palette.textMuted)
                }
            }
        }
    }

    private var quickActions: some View {
        VStack(alignment: .leading, spacing: 12) {
            SectionLabel(text: "Quick actions")
            HStack(spacing: 12) {
                NavigationLink(destination: ScannerScreen()) {
                    quickActionLabel(icon: "barcode.viewfinder", title: "Scan")
                }
                .buttonStyle(.plain)
                quickAction(icon: "mappin.and.ellipse", title: "Nearby") {
                    onSwitchTab(.nearby)
                }
                quickAction(icon: "bag", title: "Products") {
                    onSwitchTab(.products)
                }
                quickAction(icon: "fork.knife", title: "Recipes") {
                    onSwitchTab(.recipes)
                }
            }
        }
    }

    private func quickAction(icon: String, title: String, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            quickActionLabel(icon: icon, title: title)
        }
        .buttonStyle(.plain)
    }

    private func quickActionLabel(icon: String, title: String) -> some View {
        VStack(spacing: 8) {
            Image(systemName: icon)
                .font(.system(size: 22, weight: .semibold))
                .foregroundColor(Palette.leafMint)
            Text(title)
                .font(SproutlyType.labelLarge)
                .foregroundColor(Palette.textPrimary)
        }
        .frame(maxWidth: .infinity, minHeight: 84)
        .background(Palette.bgSurface)
        .overlay(RoundedRectangle(cornerRadius: 16).stroke(Palette.divider, lineWidth: 1))
        .clipShape(RoundedRectangle(cornerRadius: 16))
    }

    private var todayHighlights: some View {
        SproutlyCard {
            SectionLabel(text: "Highlights")
            highlightRow(icon: "leaf.fill", title: "New: oat milk deal nearby", subtitle: "Save 20% at GreenLeaf market")
            Divider().background(Palette.divider)
            highlightRow(icon: "sparkles", title: "Recipe of the day", subtitle: "Smoky chickpea bowl, 25 min")
            Divider().background(Palette.divider)
            highlightRow(icon: "calendar", title: "Plan your week", subtitle: "Generate a 7-day meal plan")
        }
    }

    private func highlightRow(icon: String, title: String, subtitle: String) -> some View {
        HStack(spacing: 14) {
            Image(systemName: icon)
                .font(.system(size: 18, weight: .semibold))
                .foregroundColor(Palette.leafMint)
                .frame(width: 32, height: 32)
                .background(Palette.bgDeep)
                .clipShape(Circle())
            VStack(alignment: .leading, spacing: 2) {
                Text(title).font(SproutlyType.titleMedium).foregroundColor(Palette.textPrimary)
                Text(subtitle).font(SproutlyType.bodyMedium).foregroundColor(Palette.textMuted)
            }
            Spacer()
            Image(systemName: "chevron.right").foregroundColor(Palette.textMuted)
        }
    }

    private var weeklyPreview: some View {
        SproutlyCard {
            HStack {
                SectionLabel(text: "This week")
                Spacer()
                NavigationLink("Open plan", destination: MealPlanScreen())
                    .font(SproutlyType.labelLarge)
                    .foregroundColor(Palette.leafMint)
            }
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 12) {
                    ForEach(MealPlanScreen.starterDays, id: \.day) { day in
                        VStack(alignment: .leading, spacing: 4) {
                            Text(day.day.prefix(3).uppercased())
                                .font(SproutlyType.labelMedium)
                                .foregroundColor(Palette.textMuted)
                            Text(day.lunch)
                                .font(SproutlyType.titleMedium)
                                .foregroundColor(Palette.textPrimary)
                                .lineLimit(2)
                        }
                        .padding(12)
                        .frame(width: 140, alignment: .leading)
                        .background(Palette.bgDeep)
                        .overlay(RoundedRectangle(cornerRadius: 14).stroke(Palette.divider, lineWidth: 1))
                        .clipShape(RoundedRectangle(cornerRadius: 14))
                    }
                }
            }
        }
    }
}

/// Mirrors the Compose-Canvas PlantHero in Android. Simple decorative leaf.
private struct PlantHeroCanvas: View {
    var body: some View {
        Canvas { ctx, size in
            let rect = CGRect(origin: .zero, size: size)
            let bg = Path(ellipseIn: rect)
            ctx.fill(bg, with: .color(Palette.leafDeep))

            // Leaf
            var leaf = Path()
            leaf.move(to: CGPoint(x: rect.midX, y: rect.maxY - 8))
            leaf.addQuadCurve(
                to: CGPoint(x: rect.midX, y: rect.minY + 12),
                control: CGPoint(x: rect.minX, y: rect.midY)
            )
            leaf.addQuadCurve(
                to: CGPoint(x: rect.midX, y: rect.maxY - 8),
                control: CGPoint(x: rect.maxX, y: rect.midY)
            )
            ctx.fill(leaf, with: .color(Palette.leafMint))

            // Stem
            var stem = Path()
            stem.move(to: CGPoint(x: rect.midX, y: rect.maxY - 8))
            stem.addLine(to: CGPoint(x: rect.midX, y: rect.midY + 12))
            ctx.stroke(stem, with: .color(Palette.bgDeep), lineWidth: 3)
        }
    }
}
