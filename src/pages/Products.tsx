import SectionHeader from "../components/ui/SectionHeader";
import PlaceholderCard from "../components/ui/PlaceholderCard";

// Products tab — curated plant-based products, deals, scanner CTA.
export default function Products() {
  return (
    <div className="space-y-6">
      <div>
        <p className="text-sm font-medium text-charcoal/60">Deals and product intel</p>
        <h1 className="text-3xl font-black text-ink">Products</h1>
      </div>

      <div className="no-scrollbar -mx-5 flex gap-2 overflow-x-auto px-5">
        {["All", "Pantry", "Dairy-free", "Snacks", "Frozen", "Drinks"].map(
          (c) => (
            <button
              key={c}
              className="shrink-0 rounded-full bg-white px-4 py-1.5 text-xs font-bold text-ink ring-1 ring-line transition hover:ring-ink/20"
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
            tone="cyan"
          />
          <PlaceholderCard title="Vegan cheese" subtitle="€4.10 · -10%" />
          <PlaceholderCard title="Lentil pasta" subtitle="€1.90 · -20%" tone="violet" />
        </div>
      </section>
    </div>
  );
}
