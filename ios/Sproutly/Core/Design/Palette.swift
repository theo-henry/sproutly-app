import SwiftUI

// Mirrors android/.../core/design/Color.kt — keep hex values in sync.
enum Palette {
    static let bgDeep     = Color(hex: 0x06120D)
    static let bgSurface  = Color(hex: 0x0E1E15)
    static let bgElevated = Color(hex: 0x14291E)
    static let leafMint   = Color(hex: 0x7CE7B2)
    static let leafGreen  = Color(hex: 0x2EBD7E)
    static let leafDeep   = Color(hex: 0x13503A)
    static let textPrimary = Color(hex: 0xE8F5EC)
    static let textMuted   = Color(hex: 0xA9C2B2)
    static let divider     = Color(hex: 0x1E3527)
    static let warning     = Color(hex: 0xE2C36B)
    static let error       = Color(hex: 0xE07A6A)
}

extension Color {
    init(hex: UInt32, alpha: Double = 1.0) {
        let r = Double((hex >> 16) & 0xFF) / 255.0
        let g = Double((hex >> 8) & 0xFF) / 255.0
        let b = Double(hex & 0xFF) / 255.0
        self.init(.sRGB, red: r, green: g, blue: b, opacity: alpha)
    }
}
