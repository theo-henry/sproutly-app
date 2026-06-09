import { useEffect, useMemo, useState } from "react";
import { CalendarDays, Save, Sparkles } from "lucide-react";
import { PageTitle, Reveal, Stagger, StaggerItem } from "../components/ui/Motion";
import { useAuth } from "../auth/AuthContext";
import type { MealPlan as MealPlanType, MealSlot } from "../types";
import {
  MEAL_SLOTS,
  buildGeneratedMealPlan,
  getCurrentWeekStartISO,
  loadMealPlan,
  saveMealPlan,
} from "../lib/mealPlans";

const SLOT_LABELS: Record<MealSlot, string> = {
  breakfast: "Breakfast",
  lunch: "Lunch",
  dinner: "Dinner",
  snack: "Snack",
};

const dayFormatter = new Intl.DateTimeFormat("en", {
  weekday: "short",
  month: "short",
  day: "numeric",
});

export default function MealPlan() {
  const { user } = useAuth();
  const weekStartISO = useMemo(() => getCurrentWeekStartISO(), []);
  const [plan, setPlan] = useState<MealPlanType | null>(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [status, setStatus] = useState<string | null>(null);

  useEffect(() => {
    if (!user) return undefined;

    let mounted = true;
    setLoading(true);
    setError(null);

    loadMealPlan(user.id, weekStartISO)
      .then((mealPlan) => {
        if (!mounted) return;
        setPlan(mealPlan);
      })
      .catch((loadError) => {
        if (!mounted) return;
        setError(loadError instanceof Error ? loadError.message : "Could not load meal plan.");
      })
      .finally(() => {
        if (!mounted) return;
        setLoading(false);
      });

    return () => {
      mounted = false;
    };
  }, [user, weekStartISO]);

  async function persistMealPlan(nextPlan: MealPlanType, nextStatus: string) {
    if (!user) return;

    setSaving(true);
    setError(null);
    setStatus(null);

    try {
      await saveMealPlan(user.id, nextPlan);
      setStatus(nextStatus);
    } catch (saveError) {
      setError(saveError instanceof Error ? saveError.message : "Could not save meal plan.");
    } finally {
      setSaving(false);
    }
  }

  function updateMeal(dayIndex: number, slot: MealSlot, value: string) {
    setPlan((current) => {
      if (!current) return current;

      return {
        ...current,
        days: current.days.map((day, index) =>
          index === dayIndex
            ? {
                ...day,
                meals: {
                  ...day.meals,
                  [slot]: value,
                },
              }
            : day,
        ),
      };
    });
    setStatus(null);
  }

  async function handleGenerateWeek() {
    const generatedPlan = buildGeneratedMealPlan(weekStartISO);
    setPlan(generatedPlan);
    await persistMealPlan(generatedPlan, "Generated and saved this week.");
  }

  async function handleSave() {
    if (!plan) return;
    await persistMealPlan(plan, "Meal plan saved.");
  }

  return (
    <div className="space-y-8">
      <PageTitle
        eyebrow="Meal plan"
        title="Your week, saved to your account."
        body="Generate a starting plan, adjust each slot, and keep the week tied to your Sproutly login."
      />

      <Reveal mode="scale">
        <section className="grid gap-3 rounded-[1.5rem] border border-line/70 bg-panel-soft/60 p-4">
          <div className="flex items-center gap-3 text-charcoal">
            <CalendarDays className="h-4 w-4 text-leaf" />
            <span className="text-xs font-black uppercase tracking-[0.2em]">
              Week of {weekStartISO}
            </span>
          </div>
          <div className="grid grid-cols-2 gap-3">
            <button
              type="button"
              onClick={handleGenerateWeek}
              disabled={loading || saving}
              className="flex min-h-14 items-center justify-center gap-2 rounded-2xl border border-leaf/30 bg-leaf px-4 py-3 text-sm font-black text-void transition hover:bg-mint disabled:cursor-not-allowed disabled:opacity-50"
            >
              <Sparkles className="h-4 w-4" />
              Generate
            </button>
            <button
              type="button"
              onClick={handleSave}
              disabled={loading || saving || !plan}
              className="flex min-h-14 items-center justify-center gap-2 rounded-2xl border border-leaf/35 bg-leaf/12 px-4 py-3 text-sm font-black text-mint transition hover:bg-leaf/18 disabled:cursor-not-allowed disabled:opacity-50"
            >
              <Save className="h-4 w-4" />
              {saving ? "Saving" : "Save"}
            </button>
          </div>
          {error ? (
            <div className="rounded-2xl border border-red-300/30 bg-red-400/10 p-3 text-sm font-bold leading-5 text-red-100">
              {error}
            </div>
          ) : null}
          {status ? (
            <div className="rounded-2xl border border-leaf/30 bg-leaf/10 p-3 text-sm font-bold leading-5 text-mint">
              {status}
            </div>
          ) : null}
        </section>
      </Reveal>

      {loading ? (
        <div className="grid gap-3">
          {Array.from({ length: 3 }, (_item, index) => (
            <div
              key={index}
              className="h-44 animate-pulse rounded-[1.5rem] border border-line/60 bg-panel-soft/45"
            />
          ))}
        </div>
      ) : plan ? (
        <Stagger className="grid gap-3">
          {plan.days.map((day, dayIndex) => (
            <StaggerItem key={day.date} mode={dayIndex % 2 === 0 ? "rise" : "drift"}>
              <section className="rounded-[1.5rem] border border-line/70 bg-panel-soft/58 p-4 shadow-[0_18px_70px_-50px_black]">
                <div className="mb-4 flex items-center justify-between gap-3">
                  <h2 className="text-lg font-black text-ink">
                    {dayFormatter.format(new Date(`${day.date}T12:00:00`))}
                  </h2>
                  <span className="rounded-full border border-leaf/25 bg-leaf/10 px-3 py-1 text-[0.65rem] font-black uppercase tracking-[0.16em] text-leaf">
                    {dayIndex === 0 ? "Start" : `Day ${dayIndex + 1}`}
                  </span>
                </div>
                <div className="grid gap-3">
                  {MEAL_SLOTS.map((slot) => (
                    <label key={slot} className="grid gap-2 text-xs font-black uppercase tracking-[0.16em] text-charcoal">
                      {SLOT_LABELS[slot]}
                      <input
                        value={day.meals[slot] ?? ""}
                        onChange={(event) =>
                          updateMeal(dayIndex, slot, event.target.value)
                        }
                        className="rounded-2xl border border-line/70 bg-void/45 px-4 py-3 text-sm font-bold normal-case tracking-normal text-ink outline-none transition placeholder:text-charcoal/45 focus:border-leaf/60 focus:ring-2 focus:ring-leaf/20"
                        placeholder={`Add ${SLOT_LABELS[slot].toLowerCase()}`}
                      />
                    </label>
                  ))}
                </div>
              </section>
            </StaggerItem>
          ))}
        </Stagger>
      ) : null}
    </div>
  );
}
