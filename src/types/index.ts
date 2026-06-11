// Shared domain types. Extend as the app grows.

export type Product = {
  id: string;
  name: string;
  brand: string;
  price: number;
  currency: "EUR" | "USD";
  tags: string[];
  imageUrl?: string;
};

export type Recipe = {
  id: string;
  title: string;
  description: string;
  minutes: number;
  prepMinutes: number;
  cookMinutes: number;
  servings: number;
  kcal: number;
  proteinGrams: number;
  carbsGrams: number;
  fatGrams: number;
  fiberGrams: number;
  difficulty: "Easy" | "Medium";
  mealType: "Breakfast" | "Lunch" | "Dinner" | "Snack";
  ingredients: string[];
  steps: string[];
  tags: string[];
  dietLabels: (
    | "Vegan"
    | "Vegetarian"
    | "Mostly plant-based"
    | "Flexitarian"
    | "Whole-food plant-based"
  )[];
  allergens: string[];
  equipment: string[];
  imageUrl?: string;
};

export type Place = {
  id: string;
  name: string;
  kind: "restaurant" | "supermarket";
  fullyPlantBased: boolean;
  distanceKm: number;
  lat: number;
  lng: number;
};

export type MealSlot = "breakfast" | "lunch" | "dinner" | "snack";
export type MealPlan = {
  weekStartISO: string;
  days: { date: string; meals: Partial<Record<MealSlot, Recipe["id"]>> }[];
};
