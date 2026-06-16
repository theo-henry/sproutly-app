import Foundation
import MapKit

struct NearbyPlace: Identifiable, Equatable {
    let id = UUID()
    let name: String
    let category: String
    let coordinate: CLLocationCoordinate2D
    let distanceMeters: CLLocationDistance

    static func == (lhs: NearbyPlace, rhs: NearbyPlace) -> Bool { lhs.id == rhs.id }
}

/// Mirrors android/.../nearby Overpass logic but uses MKLocalSearch on iOS.
/// Two passes: 5 km then 10 km if fewer than five matches.
@MainActor
final class PlaceSearch: ObservableObject {
    @Published private(set) var places: [NearbyPlace] = []
    @Published private(set) var isSearching: Bool = false
    @Published private(set) var lastError: String?

    func search(near coordinate: CLLocationCoordinate2D, supermarketMode: Bool, hint: String?) async {
        isSearching = true
        defer { isSearching = false }
        lastError = nil

        let queries: [String]
        if let hint, !hint.isEmpty {
            queries = [hint]
        } else if supermarketMode {
            queries = ["vegan supermarket", "plant based grocery", "organic market"]
        } else {
            queries = ["vegan restaurant", "plant based cafe", "vegetarian restaurant"]
        }

        var radius: CLLocationDistance = 5_000
        var results: [NearbyPlace] = []
        for _ in 0..<2 {
            results = await runQueries(queries, near: coordinate, radius: radius)
            if results.count >= 5 { break }
            radius = 10_000
        }
        places = results
            .sorted { $0.distanceMeters < $1.distanceMeters }
    }

    private func runQueries(_ queries: [String], near coordinate: CLLocationCoordinate2D, radius: CLLocationDistance) async -> [NearbyPlace] {
        var out: [NearbyPlace] = []
        let center = CLLocation(latitude: coordinate.latitude, longitude: coordinate.longitude)
        for q in queries {
            let request = MKLocalSearch.Request()
            request.naturalLanguageQuery = q
            request.region = MKCoordinateRegion(
                center: coordinate,
                latitudinalMeters: radius * 2,
                longitudinalMeters: radius * 2
            )
            do {
                let response = try await MKLocalSearch(request: request).start()
                for item in response.mapItems {
                    let coord = item.placemark.coordinate
                    let dist = center.distance(from: CLLocation(latitude: coord.latitude, longitude: coord.longitude))
                    guard dist <= radius else { continue }
                    let name = item.name ?? "Unnamed place"
                    if out.contains(where: { $0.name == name && abs($0.distanceMeters - dist) < 1 }) { continue }
                    out.append(NearbyPlace(
                        name: name,
                        category: q.capitalized,
                        coordinate: coord,
                        distanceMeters: dist
                    ))
                }
            } catch {
                lastError = error.localizedDescription
            }
        }
        return out
    }
}
