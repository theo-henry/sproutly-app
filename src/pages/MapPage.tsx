import PlaceholderCard from "../components/ui/PlaceholderCard";
import { motion } from "framer-motion";
import { PageTitle, Reveal, Stagger, StaggerItem } from "../components/ui/Motion";

// Nearby tab — supermarkets + restaurants on a map.
// Map library (Leaflet / Mapbox / Google) gets wired in later;
// for the MVP we render a styled placeholder + a list of pins.
export default function MapPage() {
  return (
    <div className="space-y-8">
      <PageTitle
        eyebrow="Nearby"
        title="A cleaner map for plant-based options."
        body="Restaurants and supermarkets around you, separated by fully plant-based and plant-friendly."
      />

      <Reveal mode="scale">
        <div className="dark-grid relative h-[26rem] overflow-hidden rounded-[1.5rem] border border-line/70 bg-void">
          <motion.div
            className="absolute left-10 top-8 h-44 w-px bg-gradient-to-b from-transparent via-leaf/70 to-transparent"
            animate={{ y: [0, 28, 0] }}
            transition={{ duration: 5, repeat: Infinity, ease: "easeInOut" }}
          />
          <motion.div
            className="absolute left-1/4 top-1/3 h-28 w-28 rounded-full border border-leaf/20"
            animate={{ scale: [1, 1.25, 1], opacity: [0.4, 0.12, 0.4] }}
            transition={{ duration: 4, repeat: Infinity, ease: "easeInOut" }}
          />
          {[
            "right-12 top-16 bg-leaf",
            "bottom-14 left-16 bg-mint",
            "bottom-20 right-24 bg-sage",
            "left-[48%] top-[52%] bg-lichen",
          ].map((pin) => (
            <motion.div
              key={pin}
              className={`absolute h-3 w-3 rounded-full ${pin} shadow-[0_0_28px_oklch(0.74_0.12_142/0.55)]`}
              animate={{ scale: [1, 1.35, 1] }}
              transition={{ duration: 2.8, repeat: Infinity, ease: "easeInOut" }}
            />
          ))}
          <div className="absolute inset-0 grid place-items-center text-center text-ink/80">
            <div>
              <div className="text-lg font-black">Map goes here</div>
              <div className="text-xs text-charcoal">
                Drop in Leaflet / Mapbox / Google Maps
              </div>
            </div>
          </div>
        </div>
      </Reveal>

      <Stagger className="grid gap-3 lg:grid-cols-3">
        <StaggerItem mode="drift">
          <PlaceholderCard tone="leaf" title="Green Garden Bistro" subtitle="100% plant-based · 0.4 km" meta="Open" />
        </StaggerItem>
        <StaggerItem mode="rise">
          <PlaceholderCard tone="moss" title="BioMarket Centre" subtitle="Supermarket · 0.8 km" meta="Groceries" />
        </StaggerItem>
        <StaggerItem mode="scale">
          <PlaceholderCard title="Sprout & Co." subtitle="Vegan options · 1.2 km" meta="Restaurant" />
        </StaggerItem>
      </Stagger>
    </div>
  );
}
