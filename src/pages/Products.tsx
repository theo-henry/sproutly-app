import SectionHeader from "../components/ui/SectionHeader";
import PlaceholderCard from "../components/ui/PlaceholderCard";
import { PageTitle, Reveal, Stagger, StaggerItem } from "../components/ui/Motion";

// Products tab — curated plant-based products, deals, scanner CTA.
export default function Products() {
  return (
    <div className="space-y-8">
      <PageTitle
        eyebrow="Products"
        title="Deals, labels, and cleaner choices."
        body="Curated plant-based products with the information that matters before you buy."
      />

      <Reveal mode="drift">
        <div className="no-scrollbar -mx-5 flex gap-2 overflow-x-auto px-5 sm:mx-0 sm:px-0">
        {["All", "Pantry", "Dairy-free", "Snacks", "Frozen", "Drinks"].map(
          (c) => (
            <button
              key={c}
              className="shrink-0 rounded-full border border-line/70 bg-panel-soft/70 px-4 py-2 text-xs font-black text-charcoal transition hover:border-leaf/40 hover:bg-leaf/12 hover:text-mint"
            >
              {c}
            </button>
          ),
        )}
        </div>
      </Reveal>

      <section>
        <SectionHeader title="Deals this week" />
        <Stagger className="grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-4">
          <StaggerItem mode="scale">
            <PlaceholderCard title="Oat milk barista" subtitle="€2.49 · -25%" tone="leaf" meta="Deal" />
          </StaggerItem>
          <StaggerItem mode="rise">
            <PlaceholderCard title="Tempeh block" subtitle="€3.20 · -15%" tone="moss" meta="Protein" />
          </StaggerItem>
          <StaggerItem mode="drift">
            <PlaceholderCard title="Vegan cheese" subtitle="€4.10 · -10%" tone="sage" meta="New" />
          </StaggerItem>
          <StaggerItem mode="scale">
            <PlaceholderCard title="Lentil pasta" subtitle="€1.90 · -20%" tone="mint" meta="Pantry" />
          </StaggerItem>
        </Stagger>
      </section>

      <Reveal mode="wipe">
        <section className="rounded-[1.5rem] border border-line/70 bg-panel-soft/60 p-5">
          <p className="text-xs font-black uppercase tracking-[0.24em] text-leaf/75">
            Label scanner
          </p>
          <h2 className="mt-3 text-2xl font-black text-ink">Ingredient clarity before checkout.</h2>
          <p className="mt-3 max-w-2xl text-sm leading-6 text-charcoal">
            Scan a product and surface vegan status, allergens, nutrition flags,
            and better nearby alternatives.
          </p>
        </section>
      </Reveal>
    </div>
  );
}
