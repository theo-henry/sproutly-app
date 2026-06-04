import SectionHeader from "../components/ui/SectionHeader";
import PlaceholderCard from "../components/ui/PlaceholderCard";

// Products tab — curated plant-based products, deals, scanner CTA.
export default function Products() {
  return (
    <div className="space-y-6">
      <h1 className="font-display text-3xl font-extrabold text-forest">
        Products
      </h1>

      <div className="no-scrollbar -mx-5 flex gap-2 overflow-x-auto px-5">
        {["All", "Pantry", "Dairy-free", "Snacks", "Frozen", "Drinks"].map(
          (c) => (
            <button
              key={c}
              className="shrink-0 rounded-full bg-white px-4 py-1.5 text-xs font-semibold text-forest ring-1 ring-black/5"
            >
              {c}
            </button>
          ),
        )}
      </div>

      <section>
        <SectionHeader title="Deals this week" />
        <div className="grid grid-cols-2 gap-3">
          <PlaceholderCard
            title="Oat milk barista"
            subtitle="€2.49 · -25%"
            tone="leaf"
          />
          <PlaceholderCard
            title="Tempeh block"
            subtitle="€3.20 · -15%"
            tone="clay"
          />
          <PlaceholderCard title="Vegan cheese" subtitle="€4.10 · -10%" />
          <PlaceholderCard title="Lentil pasta" subtitle="€1.90 · -20%" />
        </div>
      </section>
    </div>
  );
}
