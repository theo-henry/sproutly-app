import SwiftUI

struct SproutlyCard<Content: View>: View {
    var accent: Bool = false
    @ViewBuilder var content: () -> Content

    var body: some View {
        VStack(alignment: .leading, spacing: 12, content: content)
            .padding(18)
            .background(accent ? Palette.bgElevated : Palette.bgSurface)
            .overlay(
                RoundedRectangle(cornerRadius: 20).stroke(Palette.divider, lineWidth: 1)
            )
            .clipShape(RoundedRectangle(cornerRadius: 20))
    }
}

struct SectionLabel: View {
    let text: String
    var body: some View {
        Text(text.uppercased())
            .font(SproutlyType.labelMedium)
            .kerning(0.5)
            .foregroundColor(Palette.textMuted)
    }
}

struct MintPillButton: View {
    let label: String
    var enabled: Bool = true
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Text(label)
                .font(SproutlyType.titleMedium.weight(.semibold))
                .frame(maxWidth: .infinity, minHeight: 52)
        }
        .disabled(!enabled)
        .background(enabled ? Palette.leafMint : Palette.leafMint.opacity(0.5))
        .foregroundColor(Palette.bgDeep)
        .clipShape(RoundedRectangle(cornerRadius: 16))
    }
}

struct GhostButton: View {
    let label: String
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Text(label)
                .font(SproutlyType.titleMedium)
                .frame(maxWidth: .infinity, minHeight: 52)
        }
        .foregroundColor(Palette.textPrimary)
        .overlay(
            RoundedRectangle(cornerRadius: 16).stroke(Palette.divider, lineWidth: 1)
        )
    }
}

struct SproutlyChip: View {
    let label: String
    let selected: Bool
    let onTap: () -> Void

    var body: some View {
        Button(action: onTap) {
            Text(label)
                .font(SproutlyType.labelLarge)
                .padding(.horizontal, 14)
                .padding(.vertical, 8)
                .foregroundColor(selected ? Palette.bgDeep : Palette.textPrimary)
                .background(selected ? Palette.leafMint : Color.clear)
                .overlay(
                    Capsule().stroke(selected ? Palette.leafMint : Palette.divider, lineWidth: 1)
                )
                .clipShape(Capsule())
        }
        .buttonStyle(.plain)
    }
}

struct ScreenBackground<Content: View>: View {
    @ViewBuilder var content: () -> Content
    var body: some View {
        ZStack {
            Palette.bgDeep.ignoresSafeArea()
            content()
        }
    }
}
