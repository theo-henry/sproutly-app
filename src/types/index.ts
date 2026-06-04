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
  minutes: number;
  servings: number;
  kcal: number;
  proteinGrams: number;
  ingredients: string[];
  steps: string[];
  tags: string[];
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
