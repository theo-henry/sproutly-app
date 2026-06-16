import SwiftUI

struct ProductsScreen: View {
    let onOpenNearbyStore: (String) -> Void

    private let categories = [
        "Dairy alternatives", "Meat alternatives", "Snacks", "Pantry",
        "Frozen", "Bakery", "Drinks", "Supplements"
    ]

    private let deals: [Deal] = [
        Deal(store: "GreenLeaf", title: "Oat milk 1L", subtitle: "20% off this week", price: "€1.79"),
        Deal(store: "Veggo Express", title: "Tempeh strips", subtitle: "2 for €5", price: "€2.50"),
        Deal(store: "Plant Pantry", title: "Chickpea pasta", subtitle: "New arrival", price: "€3.20"),
    ]

    var body: some View {
        ScreenBackground {
            ScrollView {
                VStack(alignment: .leading, spacing: 20) {
                    Text("Products")
                        .font(SproutlyType.displaySmall)
                        .foregroundColor(Palette.textPrimary)
                        .padding(.top, 8)

                    SproutlyCard(accent: true) {
                        SectionLabel(text: "Scan to verify")
                        Text("Quick-check ingredients and certifications by scanning a barcode.")
                            .font(SproutlyType.bodyMedium)
                            .foregroundColor(Palette.textMuted)
                        NavigationLink(destination: ScannerScreen()) {
                            HStack {
                                Image(systemName: "barcode.viewfinder")
                                Text("Open scanner")
                            }
                            .font(SproutlyType.titleMedium.weight(.semibold))
                            .frame(maxWidth: .infinity, minHeight: 52)
                            .background(Palette.leafMint)
                            .foregroundColor(Palette.bgDeep)
                            .clipShape(RoundedRectangle(cornerRadius: 16))
                        }
                    }

                    SproutlyCard {
                        SectionLabel(text: "Categories")
                        LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 10) {
                            ForEach(categories, id: \.self) { cat in
                                Text(cat)
                                    .font(SproutlyType.titleMedium)
                                    .foregroundColor(Palette.textPrimary)
                                    .frame(maxWidth: .infinity, minHeight: 56)
                                    .background(Palette.bgDeep)
                                    .overlay(RoundedRectangle(cornerRadius: 14).stroke(Palette.divider, lineWidth: 1))
                                    .clipShape(RoundedRectangle(cornerRadius: 14))
                            }
                        }
                    }

                    SproutlyCard {
                        SectionLabel(text: "Deals near you")
                        ForEach(deals) { deal in
                            VStack(spacing: 0) {
                                HStack(spacing: 12) {
                                    VStack(alignment: .leading, spacing: 4) {
                                        Text(deal.title).font(SproutlyType.titleMedium).foregroundColor(Palette.textPrimary)
                                        Text(deal.subtitle).font(SproutlyType.bodyMedium).foregroundColor(Palette.textMuted)
                                        Text(deal.store).font(SproutlyType.labelMedium).foregroundColor(Palette.leafMint)
                                    }
                                    Spacer()
                                    VStack(alignment: .trailing, spacing: 8) {
                                        Text(deal.price).font(SproutlyType.titleMedium).foregroundColor(Palette.textPrimary)
                                        Button {
                                            onOpenNearbyStore(deal.store)
                                        } label: {
                                            Text("Find store")
                                                .font(SproutlyType.labelLarge)
                                                .padding(.horizontal, 12)
                                                .padding(.vertical, 6)
                                                .background(Palette.bgDeep)
                                                .overlay(Capsule().stroke(Palette.divider, lineWidth: 1))
                                                .foregroundColor(Palette.leafMint)
                                                .clipShape(Capsule())
                                        }
                                        .buttonStyle(.plain)
                                    }
                                }
                                .padding(.vertical, 12)
                                if deal.id != deals.last?.id {
                                    Divider().background(Palette.divider)
                                }
                            }
                        }
                    }
                }
                .padding(.horizontal, 20)
                .padding(.bottom, 24)
            }
        }
        .navigationBarTitleDisplayMode(.inline)
    }
}

struct Deal: Identifiable {
    let id = UUID()
    let store: String
    let title: String
    let subtitle: String
    let price: String
}
