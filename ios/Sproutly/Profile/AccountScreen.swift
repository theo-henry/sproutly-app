import SwiftUI

struct AccountScreen: View {
    @EnvironmentObject var auth: AuthViewModel
    @State private var displayName: String = ""
    @State private var diet: Diet = .vegan
    @State private var tags: Set<String> = ["high-protein", "quick"]

    private let allTags = ["high-protein", "gluten-free", "quick", "soy-free", "kid-friendly", "budget", "batch-cook"]

    enum Diet: String, CaseIterable, Identifiable {
        case vegan, vegetarian, flexitarian
        var id: String { rawValue }
        var label: String { rawValue.capitalized }
    }

    var body: some View {
        ScreenBackground {
            ScrollView {
                VStack(alignment: .leading, spacing: 16) {
                    SproutlyCard(accent: true) {
                        SectionLabel(text: "Profile")
                        VStack(alignment: .leading, spacing: 6) {
                            Text("Display name").font(SproutlyType.labelLarge).foregroundColor(Palette.textMuted)
                            TextField("", text: $displayName)
                                .font(SproutlyType.bodyLarge)
                                .foregroundColor(Palette.textPrimary)
                                .padding(.horizontal, 12).padding(.vertical, 10)
                                .background(Palette.bgDeep)
                                .overlay(RoundedRectangle(cornerRadius: 12).stroke(Palette.divider, lineWidth: 1))
                                .clipShape(RoundedRectangle(cornerRadius: 12))
                        }
                        if case let .signedIn(user) = auth.state {
                            Text(user.email).font(SproutlyType.bodyMedium).foregroundColor(Palette.textMuted)
                        }
                        MintPillButton(label: "Save") {
                            auth.updateDisplayName(displayName)
                        }
                    }

                    SproutlyCard {
                        SectionLabel(text: "Diet preference")
                        HStack(spacing: 8) {
                            ForEach(Diet.allCases) { d in
                                SproutlyChip(label: d.label, selected: diet == d) { diet = d }
                            }
                        }
                    }

                    SproutlyCard {
                        SectionLabel(text: "Tags")
                        FlowLayout(spacing: 8) {
                            ForEach(allTags, id: \.self) { tag in
                                SproutlyChip(label: tag, selected: tags.contains(tag)) {
                                    if tags.contains(tag) { tags.remove(tag) } else { tags.insert(tag) }
                                }
                            }
                        }
                    }

                    SproutlyCard {
                        SectionLabel(text: "Plan")
                        NavigationLink(destination: MealPlanScreen()) {
                            HStack {
                                Image(systemName: "calendar")
                                Text("Open meal plan")
                                Spacer()
                                Image(systemName: "chevron.right")
                            }
                            .font(SproutlyType.titleMedium)
                            .foregroundColor(Palette.textPrimary)
                            .padding(.vertical, 8)
                        }
                    }

                    GhostButton(label: "Sign out") {
                        auth.signOut()
                    }
                }
                .padding(.horizontal, 20)
                .padding(.vertical, 16)
            }
        }
        .navigationTitle("Account")
        .navigationBarTitleDisplayMode(.inline)
        .onAppear {
            if case let .signedIn(user) = auth.state, displayName.isEmpty {
                displayName = user.displayName
            }
        }
    }
}

/// Minimal flow layout for chips. iOS 16+ supports Layout protocol.
struct FlowLayout: Layout {
    var spacing: CGFloat = 8

    func sizeThatFits(proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) -> CGSize {
        let maxWidth = proposal.width ?? .infinity
        var x: CGFloat = 0
        var y: CGFloat = 0
        var rowHeight: CGFloat = 0
        for subview in subviews {
            let size = subview.sizeThatFits(.unspecified)
            if x + size.width > maxWidth {
                x = 0
                y += rowHeight + spacing
                rowHeight = 0
            }
            x += size.width + spacing
            rowHeight = max(rowHeight, size.height)
        }
        return CGSize(width: maxWidth == .infinity ? x : maxWidth, height: y + rowHeight)
    }

    func placeSubviews(in bounds: CGRect, proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) {
        var x = bounds.minX
        var y = bounds.minY
        var rowHeight: CGFloat = 0
        for subview in subviews {
            let size = subview.sizeThatFits(.unspecified)
            if x + size.width > bounds.maxX {
                x = bounds.minX
                y += rowHeight + spacing
                rowHeight = 0
            }
            subview.place(at: CGPoint(x: x, y: y), proposal: .init(size))
            x += size.width + spacing
            rowHeight = max(rowHeight, size.height)
        }
    }
}
