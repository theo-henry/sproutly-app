import SwiftUI
import MapKit
import CoreLocation

struct NearbyScreen: View {
    let initialSupermarketMode: Bool
    let productStoreHint: String?

    @StateObject private var location = LocationManager()
    @StateObject private var search = PlaceSearch()
    @State private var supermarketMode: Bool
    @State private var cameraPosition: MapCameraPosition = .region(
        MKCoordinateRegion(
            center: CLLocationCoordinate2D(latitude: 40.4168, longitude: -3.7038), // Madrid fallback
            latitudinalMeters: 5_000,
            longitudinalMeters: 5_000
        )
    )

    init(initialSupermarketMode: Bool, productStoreHint: String?) {
        self.initialSupermarketMode = initialSupermarketMode
        self.productStoreHint = productStoreHint
        _supermarketMode = State(initialValue: initialSupermarketMode)
    }

    var body: some View {
        ScreenBackground {
            VStack(spacing: 0) {
                header
                modeRow
                mapView
                resultsList
            }
        }
        .navigationBarTitleDisplayMode(.inline)
        .onAppear {
            location.requestPermission()
            location.startUpdates()
            Task { await runSearch() }
        }
        .onChange(of: location.lastLocation?.coordinate.latitude) { _, _ in
            recenterAndSearch()
        }
        .onChange(of: supermarketMode) { _, _ in
            Task { await runSearch() }
        }
    }

    private var header: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text("Nearby")
                .font(SproutlyType.displaySmall)
                .foregroundColor(Palette.textPrimary)
            Text(productStoreHint != nil
                 ? "Looking for \(productStoreHint!) close by"
                 : "Plant-based places within 5–10 km")
                .font(SproutlyType.bodyMedium)
                .foregroundColor(Palette.textMuted)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(.horizontal, 20)
        .padding(.top, 12)
    }

    private var modeRow: some View {
        HStack(spacing: 8) {
            SproutlyChip(label: "Restaurants", selected: !supermarketMode) { supermarketMode = false }
            SproutlyChip(label: "Supermarkets", selected: supermarketMode) { supermarketMode = true }
            Spacer()
            if search.isSearching {
                ProgressView().tint(Palette.leafMint)
            }
        }
        .padding(.horizontal, 20)
        .padding(.vertical, 12)
    }

    private var mapView: some View {
        Map(position: $cameraPosition) {
            if let me = location.lastLocation?.coordinate {
                Annotation("You", coordinate: me) {
                    Circle()
                        .fill(Palette.leafMint)
                        .frame(width: 14, height: 14)
                        .overlay(Circle().stroke(Palette.bgDeep, lineWidth: 2))
                }
            }
            ForEach(search.places) { place in
                Marker(place.name, coordinate: place.coordinate)
                    .tint(Palette.leafGreen)
            }
        }
        .mapStyle(.standard(elevation: .flat))
        .frame(height: 260)
        .overlay(
            RoundedRectangle(cornerRadius: 16).stroke(Palette.divider, lineWidth: 1)
        )
        .clipShape(RoundedRectangle(cornerRadius: 16))
        .padding(.horizontal, 20)
    }

    private var resultsList: some View {
        ScrollView {
            VStack(spacing: 10) {
                if location.authorizationStatus == .denied || location.authorizationStatus == .restricted {
                    SproutlyCard {
                        SectionLabel(text: "Location off")
                        Text("Enable location in Settings to search around you. Showing Madrid by default.")
                            .font(SproutlyType.bodyMedium)
                            .foregroundColor(Palette.textMuted)
                    }
                }
                if search.places.isEmpty && !search.isSearching {
                    SproutlyCard {
                        Text("No places found yet.")
                            .font(SproutlyType.bodyMedium)
                            .foregroundColor(Palette.textMuted)
                    }
                }
                ForEach(search.places) { place in
                    SproutlyCard {
                        HStack {
                            VStack(alignment: .leading, spacing: 4) {
                                Text(place.name).font(SproutlyType.titleMedium).foregroundColor(Palette.textPrimary)
                                Text(place.category).font(SproutlyType.labelMedium).foregroundColor(Palette.leafMint)
                            }
                            Spacer()
                            Text(formatDistance(place.distanceMeters))
                                .font(SproutlyType.labelLarge)
                                .foregroundColor(Palette.textMuted)
                        }
                    }
                }
            }
            .padding(.horizontal, 20)
            .padding(.vertical, 16)
        }
    }

    private func runSearch() async {
        let coord = location.lastLocation?.coordinate
            ?? CLLocationCoordinate2D(latitude: 40.4168, longitude: -3.7038)
        await search.search(
            near: coord,
            supermarketMode: supermarketMode,
            hint: productStoreHint
        )
    }

    private func recenterAndSearch() {
        guard let coord = location.lastLocation?.coordinate else { return }
        cameraPosition = .region(MKCoordinateRegion(
            center: coord,
            latitudinalMeters: 5_000,
            longitudinalMeters: 5_000
        ))
        Task { await runSearch() }
    }

    private func formatDistance(_ meters: Double) -> String {
        if meters < 1_000 { return "\(Int(meters)) m" }
        return String(format: "%.1f km", meters / 1_000)
    }
}
