import PlaceholderCard from "../components/ui/PlaceholderCard";

// Nearby tab — supermarkets + restaurants on a map.
// Map library (Leaflet / Mapbox / Google) gets wired in later;
// for the MVP we render a styled placeholder + a list of pins.
export default function MapPage() {
  return (
    <div className="space-y-4">
      <h1 className="font-display text-3xl font-extrabold text-forest">
        Nearby
      </h1>

      <div className="relative h-72 overflow-hidden rounded-3xl bg-sage/40 ring-1 ring-black/5">
        <div className="absolute inset-0 grid place-items-center text-center text-forest/70">
          <div>
            <div className="font-display text-lg font-bold">Map goes here</div>
            <div className="text-xs">
              Drop in Leaflet / Mapbox / Google Maps
            </div>
          </div>
        </div>
      </div>

      <div className="space-y-3">
        <PlaceholderCard
          tone="leaf"
          title="Green Garden Bistro"
          subtitle="100% plant-based · 0.4 km"
        />
        <PlaceholderCard
          tone="clay"
          title="BioMarket Centre"
          subtitle="Supermarket · 0.8 km"
        />
        <PlaceholderCard
          title="Sprout & Co."
          subtitle="Vegan options · 1.2 km"
        />
      </div>
    </div>
  );
}
