import PlaceholderCard from "../components/ui/PlaceholderCard";
import { PageTitle, Reveal, Stagger, StaggerItem } from "../components/ui/Motion";

// AI-assisted meal planner. Wireframe-only for now.
const DAYS = ["Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"];

export default function MealPlan() {
  return (
    <div className="space-y-8">
      <PageTitle
        eyebrow="Meal plan"
        title="Generate the week, then keep control."
        body="Plan meals, swap dishes, and build a shopping list around goals, budget, and pantry."
      />

      <Reveal mode="scale">
        <button className="w-full rounded-[1.5rem] border border-leaf/30 bg-leaf px-5 py-5 text-left text-void shadow-[0_28px_80px_-55px_oklch(0.74_0.12_142/0.7)] transition hover:bg-mint">
          <div className="text-xl font-black">Generate week</div>
          <div className="mt-1 text-xs font-bold text-void/70">
            Tailored to your goals, budget, and pantry
          </div>
        </button>
      </Reveal>

      <Stagger className="grid gap-3 lg:grid-cols-7">
        {DAYS.map((d) => (
          <StaggerItem key={d} mode="rise">
            <PlaceholderCard
              title={d}
              subtitle="Breakfast · Lunch · Dinner"
              tone={d === "Mon" ? "leaf" : d === "Fri" ? "mint" : "sage"}
            />
          </StaggerItem>
        ))}
      </Stagger>
    </div>
  );
}
