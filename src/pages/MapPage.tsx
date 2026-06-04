import PlaceholderCard from "../components/ui/PlaceholderCard";

// Nearby tab — supermarkets + restaurants on a map.
// Map library (Leaflet / Mapbox / Google) gets wired in later;
// for the MVP we render a styled placeholder + a list of pins.
export default function MapPage() {
  return (
    <div className="space-y-4">
      <div>
        <p className="text-sm font-medium text-charcoal/60">Restaurants and supermarkets</p>
        <h1 className="text-3xl font-black text-ink">Nearby</h1>
      </div>

      <div className="dark-grid relative h-72 overflow-hidden rounded-lg bg-ink ring-1 ring-ink/10">
        <div className="absolute left-10 top-8 h-24 w-px bg-cyan/80" />
        <div className="absolute right-12 top-16 h-3 w-3 rounded-full bg-leaf shadow-[0_0_30px_oklch(0.68_0.18_150/0.8)]" />
        <div className="absolute bottom-14 left-16 h-3 w-3 rounded-full bg-cyan shadow-[0_0_30px_oklch(0.72_0.14_205/0.8)]" />
        <div className="absolute bottom-20 right-24 h-3 w-3 rounded-full bg-violet shadow-[0_0_30px_oklch(0.62_0.19_295/0.8)]" />
        <div className="absolute inset-0 grid place-items-center text-center text-white/78">
          <div>
            <div className="text-lg font-black">Map goes here</div>
            <div className="text-xs text-white/55">
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
          tone="cyan"
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
