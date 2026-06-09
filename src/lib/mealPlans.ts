import type { MealPlan, MealSlot } from "../types";
import type { Json } from "./database.types";
import { requireSupabase } from "./supabase";

const MEAL_SLOTS: MealSlot[] = ["breakfast", "lunch", "dinner", "snack"];

const GENERATED_MEALS: Record<MealSlot, string[]> = {
  breakfast: [
    "Tofu scramble with greens",
    "Overnight oats with chia",
    "Avocado toast with hemp seeds",
    "Soy yogurt protein bowl",
    "Mushroom breakfast wrap",
    "Peanut butter banana oats",
    "Tempeh hash",
  ],
  lunch: [
    "Lentil power bowl",
    "Chickpea herb wrap",
    "Quinoa edamame salad",
    "Black bean burrito bowl",
    "Sesame tofu noodles",
    "Falafel plate",
    "White bean tomato stew",
  ],
  dinner: [
    "Cashew greens pasta",
    "Miso aubergine rice",
    "Red lentil dal",
    "Mushroom tofu stir-fry",
    "Smoky tempeh tacos",
    "Coconut chickpea curry",
    "Roasted squash risotto",
  ],
  snack: [
    "B12 supplement and fruit",
    "Hummus with carrots",
    "Roasted edamame",
    "Protein smoothie",
    "Trail mix",
    "Apple with peanut butter",
    "Dark chocolate soy yogurt",
  ],
};

function formatDate(date: Date) {
  const localDate = new Date(date.getTime() - date.getTimezoneOffset() * 60000);
  return localDate.toISOString().slice(0, 10);
}

function parseDate(date: string) {
  const [year, month, day] = date.split("-").map(Number);
  return new Date(year, month - 1, day);
}

export function getCurrentWeekStartISO() {
  const date = new Date();
  const day = date.getDay();
  const distanceFromMonday = day === 0 ? -6 : 1 - day;
  date.setDate(date.getDate() + distanceFromMonday);
  date.setHours(0, 0, 0, 0);

  return formatDate(date);
}

export function buildEmptyMealPlan(weekStartISO = getCurrentWeekStartISO()): MealPlan {
  const weekStart = parseDate(weekStartISO);

  return {
    weekStartISO,
    days: Array.from({ length: 7 }, (_item, index) => {
      const date = new Date(weekStart);
      date.setDate(weekStart.getDate() + index);

      return {
        date: formatDate(date),
        meals: {},
      };
    }),
  };
}

export function buildGeneratedMealPlan(
  weekStartISO = getCurrentWeekStartISO(),
): MealPlan {
  const plan = buildEmptyMealPlan(weekStartISO);

  return {
    ...plan,
    days: plan.days.map((day, index) => ({
      ...day,
      meals: MEAL_SLOTS.reduce<Partial<Record<MealSlot, string>>>(
        (meals, slot) => ({
          ...meals,
          [slot]: GENERATED_MEALS[slot][index],
        }),
        {},
      ),
    })),
  };
}

export async function loadMealPlan(userId: string, weekStartISO: string) {
  const client = requireSupabase();
  const { data, error } = await client
    .from("meal_plans")
    .select("week_start, days")
    .eq("user_id", userId)
    .eq("week_start", weekStartISO)
    .maybeSingle();

  if (error) throw error;

  if (!data) return buildEmptyMealPlan(weekStartISO);

  return {
    weekStartISO: data.week_start,
    days: data.days as MealPlan["days"],
  };
}

export async function saveMealPlan(userId: string, mealPlan: MealPlan) {
  const client = requireSupabase();
  const { error } = await client.from("meal_plans").upsert(
    {
      user_id: userId,
      week_start: mealPlan.weekStartISO,
      days: mealPlan.days as Json,
      updated_at: new Date().toISOString(),
    },
    { onConflict: "user_id,week_start" },
  );

  if (error) throw error;
}

export { MEAL_SLOTS };
