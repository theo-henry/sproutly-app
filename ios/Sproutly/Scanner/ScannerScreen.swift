import SwiftUI
import AVFoundation

/// Mirrors android/.../scanner/ui/ScannerScreen.kt — permission gate only.
/// Real AVCaptureMetadataOutput barcode binding intentionally deferred so the
/// build stays zero-dependency. The seam is clear: replace `PermissionGrantedView`
/// with a `UIViewControllerRepresentable` that owns an `AVCaptureSession`.
struct ScannerScreen: View {
    @State private var status: AVAuthorizationStatus = AVCaptureDevice.authorizationStatus(for: .video)

    var body: some View {
        ScreenBackground {
            VStack(alignment: .leading, spacing: 16) {
                Text("Scanner")
                    .font(SproutlyType.displaySmall)
                    .foregroundColor(Palette.textPrimary)
                    .padding(.top, 8)

                switch status {
                case .authorized:
                    PermissionGrantedView()
                case .denied, .restricted:
                    permissionDenied
                case .notDetermined:
                    permissionPrompt
                @unknown default:
                    permissionPrompt
                }

                Spacer()
            }
            .padding(.horizontal, 20)
            .padding(.bottom, 24)
        }
        .navigationTitle("Scan")
        .navigationBarTitleDisplayMode(.inline)
    }

    private var permissionPrompt: some View {
        SproutlyCard(accent: true) {
            SectionLabel(text: "Camera access")
            Text("Sproutly needs the camera to scan barcodes and surface plant-based info.")
                .font(SproutlyType.bodyMedium)
                .foregroundColor(Palette.textMuted)
            MintPillButton(label: "Allow camera") {
                AVCaptureDevice.requestAccess(for: .video) { granted in
                    DispatchQueue.main.async {
                        status = granted ? .authorized : .denied
                    }
                }
            }
        }
    }

    private var permissionDenied: some View {
        SproutlyCard {
            SectionLabel(text: "Permission denied")
            Text("Open Settings to allow camera access for Sproutly.")
                .font(SproutlyType.bodyMedium)
                .foregroundColor(Palette.textMuted)
            GhostButton(label: "Open Settings") {
                if let url = URL(string: UIApplication.openSettingsURLString) {
                    UIApplication.shared.open(url)
                }
            }
        }
    }
}

private struct PermissionGrantedView: View {
    var body: some View {
        SproutlyCard {
            SectionLabel(text: "Ready")
            Text("Barcode scanner placeholder. The full AVFoundation session will be wired here, matching the Android ML Kit setup.")
                .font(SproutlyType.bodyMedium)
                .foregroundColor(Palette.textMuted)
            RoundedRectangle(cornerRadius: 16)
                .fill(Palette.bgDeep)
                .overlay(
                    Image(systemName: "barcode.viewfinder")
                        .font(.system(size: 56))
                        .foregroundColor(Palette.leafMint)
                )
                .overlay(RoundedRectangle(cornerRadius: 16).stroke(Palette.divider, lineWidth: 1))
                .frame(height: 220)
        }
    }
}
