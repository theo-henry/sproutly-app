import PlaceholderCard from "../components/ui/PlaceholderCard";

// AI-assisted meal planner. Wireframe-only for now.
const DAYS = ["Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"];

export default function MealPlan() {
  return (
    <div className="space-y-5">
      <div>
        <h1 className="font-display text-3xl font-extrabold text-forest">
          Meal plan
        </h1>
        <p className="text-sm text-forest/60">
          Generate a week, swap meals, build a shopping list.
        </p>
      </div>

      <button className="w-full rounded-2xl bg-leaf px-5 py-4 text-left text-cream">
        <div className="font-display text-lg font-bold">✨ Generate week</div>
        <div className="text-xs opacity-80">
          Tailored to your goals, budget, and pantry
        </div>
      </button>

      <div className="space-y-3">
        {DAYS.map((d) => (
          <PlaceholderCard
            key={d}
            title={d}
            subtitle="Breakfast · Lunch · Dinner"
            tone="sage"
          />
        ))}
      </div>
    </div>
  );
}
