import { createClient, type SupabaseClient } from "@supabase/supabase-js";
import type { Database } from "./database.types";

const supabaseUrl = import.meta.env.VITE_SUPABASE_URL;
const supabasePublishableKey = import.meta.env.VITE_SUPABASE_PUBLISHABLE_KEY;

export const demoCredentials = {
  email: import.meta.env.VITE_SUPABASE_DEMO_EMAIL,
  password: import.meta.env.VITE_SUPABASE_DEMO_PASSWORD,
};

export const hasDemoCredentials = Boolean(
  demoCredentials.email && demoCredentials.password,
);

export const hasSupabaseConfig = Boolean(supabaseUrl && supabasePublishableKey);

export const supabase: SupabaseClient<Database> | null = hasSupabaseConfig
  ? createClient<Database>(supabaseUrl!, supabasePublishableKey!, {
      auth: {
        persistSession: true,
        autoRefreshToken: true,
        detectSessionInUrl: true,
      },
    })
  : null;

export function requireSupabase() {
  if (!supabase) {
    throw new Error(
      "Supabase is not configured. Add VITE_SUPABASE_URL and VITE_SUPABASE_PUBLISHABLE_KEY to .env.local.",
    );
  }

  return supabase;
}
