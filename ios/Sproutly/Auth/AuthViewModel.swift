import Foundation
import Combine

enum AuthState: Equatable {
    case loading
    case signedOut
    case signedIn(user: SproutlyUser)
}

struct SproutlyUser: Equatable {
    let id: String
    let email: String
    var displayName: String
}

/// Mirrors android/.../auth/AuthViewModel.kt.
///
/// On Android this hits Supabase. On iOS the Supabase Swift SDK is not wired
/// (zero-dependency build for CI), so this is a local stub that accepts the
/// demo creds from CLAUDE.md / local.properties and any non-empty
/// email+password. The seam (sign-in/sign-out/restore) matches Android, so
/// hooking the real Supabase iOS SDK later is a localized change.
@MainActor
final class AuthViewModel: ObservableObject {
    @Published private(set) var state: AuthState = .loading
    @Published var errorMessage: String?
    @Published var isSubmitting: Bool = false

    private let storageKey = "sproutly.signedInUser"

    init() {
        restore()
    }

    func restore() {
        if let data = UserDefaults.standard.data(forKey: storageKey),
           let user = try? JSONDecoder().decode(StoredUser.self, from: data) {
            state = .signedIn(user: user.toUser())
        } else {
            state = .signedOut
        }
    }

    func signIn(email: String, password: String) {
        errorMessage = nil
        let trimmedEmail = email.trimmingCharacters(in: .whitespaces)
        guard !trimmedEmail.isEmpty, !password.isEmpty else {
            errorMessage = "Enter your email and password."
            return
        }
        isSubmitting = true
        Task {
            try? await Task.sleep(nanoseconds: 400_000_000)
            let user = SproutlyUser(
                id: UUID().uuidString,
                email: trimmedEmail,
                displayName: trimmedEmail.components(separatedBy: "@").first ?? "Sprout"
            )
            persist(user)
            isSubmitting = false
            state = .signedIn(user: user)
        }
    }

    func signInWithDemo() {
        signIn(email: "demo@sproutly.app", password: "demo-password")
    }

    func signOut() {
        UserDefaults.standard.removeObject(forKey: storageKey)
        state = .signedOut
    }

    func updateDisplayName(_ name: String) {
        guard case let .signedIn(user) = state else { return }
        var updated = user
        updated.displayName = name
        persist(updated)
        state = .signedIn(user: updated)
    }

    private func persist(_ user: SproutlyUser) {
        let stored = StoredUser(id: user.id, email: user.email, displayName: user.displayName)
        if let data = try? JSONEncoder().encode(stored) {
            UserDefaults.standard.set(data, forKey: storageKey)
        }
    }

    private struct StoredUser: Codable {
        let id: String
        let email: String
        let displayName: String
        func toUser() -> SproutlyUser {
            SproutlyUser(id: id, email: email, displayName: displayName)
        }
    }
}
