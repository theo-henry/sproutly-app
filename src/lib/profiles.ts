import type { Database } from "./database.types";
import { requireSupabase } from "./supabase";

export const DIET_OPTIONS = [
  { value: "vegan", label: "Vegan" },
  { value: "vegetarian", label: "Vegetarian" },
  { value: "mostly_plant_based", label: "Mostly plant-based" },
  { value: "flexitarian", label: "Flexitarian" },
  { value: "whole_food_plant_based", label: "Whole-food plant-based" },
  { value: "other", label: "Other" },
] as const;

export const DIET_TAG_OPTIONS = [
  "High-protein",
  "Budget-friendly",
  "Gluten-free",
  "Nut-free",
  "Soy-free",
  "Quick meals",
] as const;

export const AVATAR_BUCKET = "avatars";
export const MAX_AVATAR_SIZE_BYTES = 2 * 1024 * 1024;

export type DietPreference = (typeof DIET_OPTIONS)[number]["value"];
export type DietTag = (typeof DIET_TAG_OPTIONS)[number];
export type Profile = Database["public"]["Tables"]["profiles"]["Row"];

export type ProfileInput = {
  email: string | null;
  displayName: string;
  avatarPath: string | null;
  city: string;
  country: string;
  dietPreference: DietPreference | "";
  dietTags: DietTag[];
};

function emptyToNull(value: string) {
  const trimmed = value.trim();
  return trimmed.length > 0 ? trimmed : null;
}

function avatarExtension(file: File) {
  const mimeExtension = file.type.split("/")[1]?.replace("jpeg", "jpg");
  const nameExtension = file.name.split(".").pop()?.toLowerCase();
  return mimeExtension || nameExtension || "jpg";
}

export async function loadProfile(userId: string) {
  const client = requireSupabase();
  const { data, error } = await client
    .from("profiles")
    .select("*")
    .eq("id", userId)
    .maybeSingle();

  if (error) throw error;

  return data;
}

export async function saveProfile(userId: string, input: ProfileInput) {
  const client = requireSupabase();
  const { data, error } = await client
    .from("profiles")
    .upsert(
      {
        id: userId,
        email: input.email,
        display_name: emptyToNull(input.displayName),
        avatar_path: input.avatarPath,
        city: emptyToNull(input.city),
        country: emptyToNull(input.country),
        diet_preference: input.dietPreference || null,
        diet_tags: input.dietTags,
        updated_at: new Date().toISOString(),
      },
      { onConflict: "id" },
    )
    .select("*")
    .single();

  if (error) throw error;

  return data;
}

export async function uploadAvatar(
  userId: string,
  file: File,
  previousPath: string | null,
) {
  const client = requireSupabase();
  const path = `${userId}/avatar-${Date.now()}.${avatarExtension(file)}`;
  const { error } = await client.storage.from(AVATAR_BUCKET).upload(path, file, {
    cacheControl: "3600",
    upsert: false,
  });

  if (error) throw error;

  if (previousPath && previousPath !== path) {
    await client.storage.from(AVATAR_BUCKET).remove([previousPath]);
  }

  return path;
}

export function getAvatarUrl(path: string | null) {
  if (!path) return null;

  const client = requireSupabase();
  return client.storage.from(AVATAR_BUCKET).getPublicUrl(path).data.publicUrl;
}
