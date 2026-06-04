import { Link } from "react-router-dom";
import SectionHeader from "../components/ui/SectionHeader";
import PlaceholderCard from "../components/ui/PlaceholderCard";

// Recipes tab — browsable recipe library; entry point to meal planning.
export default function Recipes() {
  return (
    <div className="space-y-6">
      <div className="flex items-end justify-between">
        <div>
          <p className="text-sm font-medium text-charcoal/60">Cook and plan</p>
          <h1 className="text-3xl font-black text-ink">Recipes</h1>
        </div>
        <Link
          to="/meal-plan"
          className="rounded-full bg-ink px-4 py-2 text-xs font-bold text-white transition hover:bg-leaf hover:text-ink"
        >
          AI meal plan
        </Link>
      </div>

      <section>
        <SectionHeader title="Quick & easy" />
        <div className="space-y-3">
          <PlaceholderCard
            title="15-min peanut noodles"
            subtitle="One pan · 12g protein"
            tone="violet"
          />
          <PlaceholderCard
            title="Lemon herb tofu wrap"
            subtitle="10 min · high fiber"
            tone="leaf"
          />
          <PlaceholderCard
            title="Chickpea shakshuka"
            subtitle="20 min · iron-rich"
            tone="cyan"
          />
        </div>
      </section>

      <section>
        <SectionHeader title="Seasonal picks" />
        <div className="grid grid-cols-2 gap-3">
          <PlaceholderCard title="Roasted squash" tone="leaf" />
          <PlaceholderCard title="Citrus salad" tone="cyan" />
        </div>
      </section>
    </div>
  );
}
