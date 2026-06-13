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

type MealPlanDay = {
  date: string;
  meals: Record<string, string>;
};

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
};

const dayNames = ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"];

function jsonResponse(body: unknown, status = 200) {
  return new Response(JSON.stringify(body), {
    status,
    headers: {
      ...corsHeaders,
      "Content-Type": "application/json",
    },
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

function normalizeTags(profile: Profile | null) {
  return (profile?.diet_tags ?? []).map((tag) => tag.toLowerCase());
}

function buildMealPools(profile: Profile | null) {
  const tags = normalizeTags(profile);
  const isQuick = tags.some((tag) => tag.includes("quick"));
  const isBudget = tags.some((tag) => tag.includes("budget"));
  const isHighProtein = tags.some((tag) => tag.includes("protein"));

  const breakfast = isQuick
    ? [
      "Overnight oats with chia and berries",
      "Soy yogurt bowl with granola",
      "Avocado toast with hemp seeds",
      "Peanut butter banana oats",
      "Tofu scramble breakfast wrap",
      "Protein smoothie with spinach",
      "Apple cinnamon oat bowl",
    ]
    : [
      "Tofu scramble with greens",
      "Overnight oats with chia",
      "Avocado toast with hemp seeds",
      "Soy yogurt protein bowl",
      "Mushroom breakfast wrap",
      "Peanut butter banana oats",
      "Tempeh hash with potatoes",
    ];

  const lunch = isBudget
    ? [
      "Lentil power bowl with brown rice",
      "Chickpea herb wrap",
      "Black bean burrito bowl",
      "White bean tomato stew",
      "Peanut soba noodle salad",
      "Split pea soup with toast",
      "Roasted vegetable couscous",
    ]
    : [
      "Lentil power bowl",
      "Chickpea herb wrap",
      "Quinoa edamame salad",
      "Black bean burrito bowl",
      "Sesame tofu noodles",
      "Falafel plate with tabbouleh",
      "White bean tomato stew",
    ];

  const dinner = isHighProtein
    ? [
      "Mushroom tofu stir-fry",
      "Smoky tempeh tacos",
      "Red lentil dal with quinoa",
      "Seitan fajita bowl",
      "Chickpea coconut curry",
      "Black bean chili with avocado",
      "Edamame pesto pasta",
    ]
    : [
      "Cashew greens pasta",
      "Miso aubergine rice",
      "Red lentil dal",
      "Mushroom tofu stir-fry",
      "Smoky tempeh tacos",
      "Coconut chickpea curry",
      "Roasted squash risotto",
    ];

  const snack = [
    "Hummus with carrots",
    "Roasted edamame",
    "Protein smoothie",
    "Trail mix",
    "Apple with peanut butter",
    "Dark chocolate soy yogurt",
    "Fruit with pumpkin seeds",
  ];

  return { breakfast, lunch, dinner, snack };
}

function buildGeneratedMealPlan(weekStart: string, profile: Profile | null): MealPlanDay[] {
  const pools = buildMealPools(profile);
  const tags = normalizeTags(profile);
  const seedOffset = tags.join("|").length % 7;

  return Array.from({ length: 7 }, (_, index) => ({
    date: addDays(weekStart, index),
    meals: {
      breakfast: pools.breakfast[(index + seedOffset) % pools.breakfast.length],
      lunch: pools.lunch[(index + seedOffset + 2) % pools.lunch.length],
      dinner: pools.dinner[(index + seedOffset + 4) % pools.dinner.length],
      snack: pools.snack[(index + seedOffset + 1) % pools.snack.length],
    },
  }));
}

function readableDiet(profile: Profile | null) {
  return profile?.diet_preference?.replaceAll("_", " ") ?? "plant-based";
}

function formatMealPlanEmail(name: string, weekStart: string, days: MealPlanDay[], profile: Profile | null) {
  const lines = [
    `Hi ${name},`,
    "",
    `Here is your Sproutly meal plan for the week of ${weekStart}.`,
    `Diet focus: ${readableDiet(profile)}`,
    "",
  ];

  days.forEach((day, index) => {
    lines.push(`${dayNames[index]} ${day.date}`);
    lines.push(`Breakfast: ${day.meals.breakfast}`);
    lines.push(`Lunch: ${day.meals.lunch}`);
    lines.push(`Dinner: ${day.meals.dinner}`);
    lines.push(`Snack: ${day.meals.snack}`);
    lines.push("");
  });

  lines.push("Open Sproutly to edit or save changes to your plan.");
  return lines.join("\n");
}

Deno.serve(async (req) => {
  if (req.method === "OPTIONS") {
    return new Response("ok", { headers: corsHeaders });
  }

  if (req.method !== "POST") {
    return jsonResponse({ error: "Method not allowed" }, 405);
  }

  const supabaseUrl = Deno.env.get("SUPABASE_URL");
  const anonKey = Deno.env.get("SUPABASE_ANON_KEY");
  const serviceRoleKey = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY");
  const appsScriptUrl = Deno.env.get("GOOGLE_APPS_SCRIPT_EMAIL_URL");
  const appsScriptSecret = Deno.env.get("GOOGLE_APPS_SCRIPT_SECRET");

  if (!supabaseUrl || !anonKey || !serviceRoleKey || !appsScriptUrl || !appsScriptSecret) {
    return jsonResponse({ error: "Meal plan email generation is not configured." }, 500);
  }

  const authorization = req.headers.get("Authorization");
  if (!authorization) {
    return jsonResponse({ error: "Missing authorization header." }, 401);
  }

  const userClient = createClient(supabaseUrl, anonKey, {
    global: { headers: { Authorization: authorization } },
  });
  const serviceClient = createClient(supabaseUrl, serviceRoleKey, {
    auth: { persistSession: false },
  });

  const { data: authData, error: authError } = await userClient.auth.getUser();
  const user = authData?.user;
  if (authError || !user) {
    return jsonResponse({ error: "Invalid user session." }, 401);
  }
  if (!user.email) {
    return jsonResponse({ error: "Your account needs an email address to generate a meal plan." }, 400);
  }

  const body = (await req.json().catch(() => ({}))) as RequestBody;
  const weekStart = isISODate(body.week_start) ? body.week_start : currentMondayISO();

  const { data: profile, error: profileError } = await serviceClient
    .from("profiles")
    .select("display_name, city, country, diet_preference, diet_tags")
    .eq("id", user.id)
    .maybeSingle<Profile>();

  if (profileError) {
    return jsonResponse({ error: profileError.message }, 500);
  }

  const days = buildGeneratedMealPlan(weekStart, profile ?? null);

  const { error: saveError } = await serviceClient
    .from("meal_plans")
    .upsert(
      {
        user_id: user.id,
        week_start: weekStart,
        days,
      },
      { onConflict: "user_id,week_start" },
    );

  if (saveError) {
    return jsonResponse({ error: saveError.message }, 500);
  }

  const displayName = profile?.display_name?.trim() || user.email.split("@")[0];
  const emailSubject = `Your Sproutly meal plan for the week of ${weekStart}`;
  const emailBody = formatMealPlanEmail(displayName, weekStart, days, profile ?? null);
  const emailResponse = await fetch(appsScriptUrl, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      secret: appsScriptSecret,
      email: user.email,
      to: user.email,
      name: displayName,
      week_start: weekStart,
      subject: emailSubject,
      body: emailBody,
      meal_plan: emailBody,
      meal_plan_json: {
        week_start: weekStart,
        days,
      },
    }),
  });

  if (!emailResponse.ok) {
    return jsonResponse({ error: "Meal plan saved, but the email could not be sent." }, 502);
  }

  return jsonResponse({
    week_start: weekStart,
    days,
    emailed: true,
  });
});
