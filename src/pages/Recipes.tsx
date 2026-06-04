import { Link } from "react-router-dom";
import SectionHeader from "../components/ui/SectionHeader";
import PlaceholderCard from "../components/ui/PlaceholderCard";

// Recipes tab — browsable recipe library; entry point to meal planning.
export default function Recipes() {
  return (
    <div className="space-y-6">
      <div className="flex items-end justify-between">
        <h1 className="font-display text-3xl font-extrabold text-forest">
          Recipes
        </h1>
        <Link
          to="/meal-plan"
          className="rounded-full bg-leaf px-4 py-2 text-xs font-semibold text-cream"
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
            tone="clay"
          />
          <PlaceholderCard
            title="Lemon herb tofu wrap"
            subtitle="10 min · high fiber"
            tone="leaf"
          />
          <PlaceholderCard
            title="Chickpea shakshuka"
            subtitle="20 min · iron-rich"
            tone="sage"
          />
        </div>
      </section>

      <section>
        <SectionHeader title="Seasonal picks" />
        <div className="grid grid-cols-2 gap-3">
          <PlaceholderCard title="Roasted squash" />
          <PlaceholderCard title="Citrus salad" />
        </div>
      </section>
    </div>
  );
}
