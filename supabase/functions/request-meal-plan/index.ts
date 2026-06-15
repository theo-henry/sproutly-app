import { createClient } from "https://esm.sh/@supabase/supabase-js@2";

type RequestBody = {
  week_start?: string;
};

type Profile = {
  display_name: string | null;
  city: string | null;
  country: string | null;
  diet_preference: string | null;
  diet_tags: string[] | null;
};

type CatalogRow = {
  id: string;
  category: string;
  name: string;
};

type TemplateSlots = {
  breakfast: string[];
  lunch: string[];
  dinner: string[];
  snack: string[];
};

type TemplateRow = {
  id: string;
  name: string;
  diet_preference: string;
  tags: string[];
  slots: TemplateSlots;
};

type MealPlanDay = {
  date: string;
  meals: Record<string, string>;
};

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
};

// Diets that have no curated templates of their own fall back to vegan,
// which is the broadest plant-based set and safe for everyone.
const dietFallback: Record<string, string> = {
  mostly_plant_based: "vegan",
  whole_food_plant_based: "vegan",
  other: "vegan",
};

function jsonResponse(body: unknown, status = 200) {
  return new Response(JSON.stringify(body), {
    status,
    headers: { ...corsHeaders, "Content-Type": "application/json" },
  });
}

function currentMondayISO() {
  const now = new Date();
  const day = now.getUTCDay();
  const distanceFromMonday = day === 0 ? -6 : 1 - day;
  now.setUTCDate(now.getUTCDate() + distanceFromMonday);
  now.setUTCHours(0, 0, 0, 0);
  return now.toISOString().slice(0, 10);
}

function isISODate(value: string | undefined): value is string {
  return !!value && /^\d{4}-\d{2}-\d{2}$/.test(value);
}

function addDays(isoDate: string, days: number) {
  const date = new Date(`${isoDate}T00:00:00.000Z`);
  date.setUTCDate(date.getUTCDate() + days);
  return date.toISOString().slice(0, 10);
}

function normalizeTags(tags: string[] | null | undefined): string[] {
  return (tags ?? []).map((tag) => tag.toLowerCase().replace(/\s+/g, "-"));
}

function pickTemplate(templates: TemplateRow[], userTags: string[]): TemplateRow {
  // Prefer a template whose tags overlap the user's diet tags; fall back to
  // any template for the diet if no tagged template is available.
  const tagged = templates.filter((tpl) =>
    tpl.tags.some((tag) => userTags.includes(tag.toLowerCase())),
  );
  const pool = tagged.length > 0 ? tagged : templates;
  return pool[Math.floor(Math.random() * pool.length)];
}

function assembleDays(
  weekStart: string,
  template: TemplateRow,
  catalogById: Map<string, CatalogRow>,
): MealPlanDay[] {
  const resolve = (id: string) => catalogById.get(id)?.name ?? id;
  return Array.from({ length: 7 }, (_, index) => ({
    date: addDays(weekStart, index),
    meals: {
      breakfast: resolve(template.slots.breakfast[index] ?? template.slots.breakfast[0]),
      lunch:     resolve(template.slots.lunch[index]     ?? template.slots.lunch[0]),
      dinner:    resolve(template.slots.dinner[index]    ?? template.slots.dinner[0]),
      snack:     resolve(template.slots.snack[index]     ?? template.slots.snack[0]),
    },
  }));
}

Deno.serve(async (req) => {
  if (req.method === "OPTIONS") return new Response("ok", { headers: corsHeaders });
  if (req.method !== "POST") return jsonResponse({ error: "Method not allowed" }, 405);

  const supabaseUrl = Deno.env.get("SUPABASE_URL");
  const anonKey = Deno.env.get("SUPABASE_ANON_KEY");
  const serviceRoleKey = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY");

  if (!supabaseUrl || !anonKey || !serviceRoleKey) {
    return jsonResponse({ error: "Meal plan generation is not configured." }, 500);
  }

  const authorization = req.headers.get("Authorization");
  if (!authorization) return jsonResponse({ error: "Missing authorization header." }, 401);

  const userClient = createClient(supabaseUrl, anonKey, {
    global: { headers: { Authorization: authorization } },
  });
  const serviceClient = createClient(supabaseUrl, serviceRoleKey, {
    auth: { persistSession: false },
  });

  const { data: authData, error: authError } = await userClient.auth.getUser();
  const user = authData?.user;
  if (authError || !user) return jsonResponse({ error: "Invalid user session." }, 401);

  const body = (await req.json().catch(() => ({}))) as RequestBody;
  const weekStart = isISODate(body.week_start) ? body.week_start : currentMondayISO();

  const { data: profile, error: profileError } = await serviceClient
    .from("profiles")
    .select("display_name, city, country, diet_preference, diet_tags")
    .eq("id", user.id)
    .maybeSingle<Profile>();
  if (profileError) return jsonResponse({ error: profileError.message }, 500);

  const requestedDiet = (profile?.diet_preference ?? "vegan").toLowerCase();
  const templateDiet = dietFallback[requestedDiet] ?? requestedDiet;

  const { data: templates, error: templatesError } = await serviceClient
    .from("meal_plan_templates")
    .select("id, name, diet_preference, tags, slots")
    .eq("diet_preference", templateDiet)
    .returns<TemplateRow[]>();
  if (templatesError) return jsonResponse({ error: templatesError.message }, 500);

  let pool = templates ?? [];
  if (pool.length === 0) {
    // Final safety net: load vegan templates if the diet has no rows.
    const { data: veganTemplates } = await serviceClient
      .from("meal_plan_templates")
      .select("id, name, diet_preference, tags, slots")
      .eq("diet_preference", "vegan")
      .returns<TemplateRow[]>();
    pool = veganTemplates ?? [];
  }
  if (pool.length === 0) {
    return jsonResponse({ error: "No meal plan templates are configured yet." }, 500);
  }

  const userTags = normalizeTags(profile?.diet_tags);
  const template = pickTemplate(pool, userTags);

  // Collect every catalog id referenced by the chosen template and resolve in
  // a single query so we don't fan out to four lookups.
  const referencedIds = Array.from(new Set([
    ...template.slots.breakfast,
    ...template.slots.lunch,
    ...template.slots.dinner,
    ...template.slots.snack,
  ]));
  const { data: catalogRows, error: catalogError } = await serviceClient
    .from("meal_catalog")
    .select("id, category, name")
    .in("id", referencedIds)
    .returns<CatalogRow[]>();
  if (catalogError) return jsonResponse({ error: catalogError.message }, 500);

  const catalogById = new Map<string, CatalogRow>((catalogRows ?? []).map((row) => [row.id, row]));
  const days = assembleDays(weekStart, template, catalogById);

  const { error: saveError } = await serviceClient
    .from("meal_plans")
    .upsert(
      { user_id: user.id, week_start: weekStart, days },
      { onConflict: "user_id,week_start" },
    );
  if (saveError) return jsonResponse({ error: saveError.message }, 500);

  return jsonResponse({
    week_start: weekStart,
    days,
    template_id: template.id,
    template_name: template.name,
  });
});
