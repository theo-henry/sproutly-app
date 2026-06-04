import PlaceholderCard from "../components/ui/PlaceholderCard";

// AI-assisted meal planner. Wireframe-only for now.
const DAYS = ["Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"];

export default function MealPlan() {
  return (
    <div className="space-y-5">
      <div>
        <h1 className="text-3xl font-black text-ink">
          Meal plan
        </h1>
        <p className="text-sm font-medium text-charcoal/60">
          Generate a week, swap meals, build a shopping list.
        </p>
      </div>

      <button className="w-full rounded-lg bg-ink px-5 py-4 text-left text-white shadow-[0_20px_70px_-50px_black] transition hover:bg-violet">
        <div className="text-lg font-black">Generate week</div>
        <div className="text-xs text-white/70">
          Tailored to your goals, budget, and pantry
        </div>
      </button>

      <div className="space-y-3">
        {DAYS.map((d) => (
          <PlaceholderCard
            key={d}
            title={d}
            subtitle="Breakfast · Lunch · Dinner"
            tone="violet"
          />
        ))}
      </div>
    </div>
  );
}
