import SwiftUI

struct LoginScreen: View {
    @EnvironmentObject var auth: AuthViewModel
    @State private var email: String = ""
    @State private var password: String = ""

    var body: some View {
        ScreenBackground {
            ScrollView {
                VStack(alignment: .leading, spacing: 24) {
                    VStack(alignment: .leading, spacing: 8) {
                        Text("Sproutly")
                            .font(SproutlyType.displaySmall)
                            .foregroundColor(Palette.leafMint)
                        Text("Your plant-based hub for products, recipes, and places near you.")
                            .font(SproutlyType.bodyLarge)
                            .foregroundColor(Palette.textMuted)
                    }
                    .padding(.top, 24)

                    SproutlyCard {
                        SectionLabel(text: "Sign in")
                        labelledField("Email", text: $email, keyboard: .emailAddress, secure: false)
                        labelledField("Password", text: $password, keyboard: .default, secure: true)

                        if let err = auth.errorMessage {
                            Text(err)
                                .font(SproutlyType.bodyMedium)
                                .foregroundColor(Palette.error)
                        }

                        MintPillButton(label: auth.isSubmitting ? "Signing in…" : "Sign in",
                                       enabled: !auth.isSubmitting) {
                            auth.signIn(email: email, password: password)
                        }
                        GhostButton(label: "Continue with demo account") {
                            auth.signInWithDemo()
                        }
                    }

                    SproutlyCard(accent: true) {
                        SectionLabel(text: "About")
                        Text("Sproutly centralizes plant-based shopping, dining, recipes and weekly meal planning. iOS edition mirrors the Android app and shares the same Supabase backend.")
                            .font(SproutlyType.bodyMedium)
                            .foregroundColor(Palette.textMuted)
                    }
                }
                .padding(.horizontal, 20)
                .padding(.bottom, 32)
            }
        }
    }

    @ViewBuilder
    private func labelledField(_ label: String, text: Binding<String>, keyboard: UIKeyboardType, secure: Bool) -> some View {
        VStack(alignment: .leading, spacing: 6) {
            Text(label)
                .font(SproutlyType.labelLarge)
                .foregroundColor(Palette.textMuted)
            Group {
                if secure {
                    SecureField("", text: text)
                } else {
                    TextField("", text: text)
                        .keyboardType(keyboard)
                        .textInputAutocapitalization(.never)
                        .autocorrectionDisabled(true)
                }
            }
            .font(SproutlyType.bodyLarge)
            .foregroundColor(Palette.textPrimary)
            .padding(.horizontal, 14)
            .padding(.vertical, 12)
            .background(Palette.bgDeep)
            .overlay(RoundedRectangle(cornerRadius: 12).stroke(Palette.divider, lineWidth: 1))
            .clipShape(RoundedRectangle(cornerRadius: 12))
        }
    }
}
