# Sproutly — Native Android

Native Kotlin + Jetpack Compose + Material 3 client. This is a **separate** project from
the React/Vite web app at the repo root; both can coexist.

## Stack

- Kotlin 2.0, AGP 8.5, Gradle 8.9
- Jetpack Compose (BOM 2024.09.x), Material 3, Material Icons Extended
- Navigation Compose, ViewModel, Coroutines, Flow
- Supabase Kotlin SDK (Auth, Postgrest, Storage, Realtime) over Ktor OkHttp
- Coil for remote images, DataStore for prefs, Room scaffolded for future offline cache
- MapLibre Native + OpenStreetMap/Overpass + Play Services Location
- CameraX + ML Kit Barcode Scanning (placeholders wired)
- WorkManager (scaffolded for reminders)

## First-time setup

1. Open `android/` in Android Studio (Hedgehog or newer).
2. Copy `local.properties.example` → `local.properties` and fill in:
   - `sdk.dir` (Android Studio writes this on first sync)
   - `SUPABASE_URL`, `SUPABASE_ANON_KEY` (from your Supabase project)
   - `DEMO_EMAIL`, `DEMO_PASSWORD` (must exist in your Supabase auth users)
   - `MAP_STYLE_URL`, `OVERPASS_ENDPOINT` are optional map/search overrides
3. Sync Gradle, then **Run ▶** on an emulator or device (minSdk 26).

The values in `local.properties` are read at build time and emitted as
`BuildConfig.*` fields — see `app/build.gradle.kts`.

## Module layout

```
com.sproutly.app
├── MainActivity.kt
├── SproutlyApp.kt
├── navigation/        AppNavGraph, BottomNavItem, Routes
├── core/
│   ├── design/        Theme, Color, Type, Components
│   ├── config/        AppConfig (BuildConfig-backed)
│   ├── network/       SupabaseClientProvider
│   ├── result/        AppResult, UiState
│   └── permissions/   PermissionHelpers
├── auth/              Email/password + demo sign-in, session restore
├── profile/           Account screen + avatar upload + diet prefs/tags
├── home/              Dashboard + Compose-Canvas PlantHero
├── mealplan/          Editable 7-day plan, starter generator, Supabase upsert
├── recipes/           Featured/quick/seasonal placeholder feed
├── products/          Categories + deals + scanner CTA
├── nearby/            MapLibre OSM map + location-aware plant-based search
├── scanner/           Camera permission + ML Kit barcode placeholder
├── notifications/     WorkManager reminder scaffold
├── shopping/          Shopping-list models
└── ai/                AiRepository interface (backend-only Gemini)
```

## Backend assumptions

Uses the existing Supabase schema (`profiles`, `meal_plans` with unique
`(user_id, week_start)`) and an `avatars` storage bucket with per-user folder
write access.

## Google Apps Script meal-plan email

The Android app calls the `request-meal-plan` Supabase Edge Function when a user
taps **Generate a Meal Plan**. The function reads the signed-in user's profile,
builds a weekly plan from their diet preference and tags, upserts that plan into
`meal_plans`, and sends the formatted plan to the configured Google Apps Script
email endpoint.

Set this up after applying the Supabase migrations:

1. Save the Google Apps Script endpoint and shared secret as Supabase secrets:
   ```bash
   supabase secrets set GOOGLE_APPS_SCRIPT_EMAIL_URL="https://script.google.com/macros/s/.../exec"
   supabase secrets set GOOGLE_APPS_SCRIPT_SECRET="your-shared-secret"
   ```
2. Deploy the Edge Function:
   ```bash
   supabase functions deploy request-meal-plan
   ```
3. Test by signing in as a real Supabase user and tapping **Generate a Meal Plan**
   in Android.

Every generate tap saves the newest plan for that user/week and sends a fresh
email. Because `meal_plans` has a unique `(user_id, week_start)` constraint, a
second generation for the same week replaces the previous in-app plan.

## What's intentionally placeholder

These are wired with interfaces and TODOs so they can be filled in without
restructuring:

- **Scanner** — CameraX preview + ML Kit analyzer not yet bound (just permission flow).
- **AI** — `AiRepository` is an interface only. **Do not** add Gemini keys to the
  Android client; implement against a Supabase Edge Function and inject that
  impl in `SproutlyApp.onCreate()`.
- **Room** — dependency added; no DAOs yet.

## Nearby map

The Nearby tab requests fine/coarse Android location permission, centers on the
device when available, and falls back to central Madrid otherwise. It searches
OpenStreetMap/Overpass for plant-based or plant-friendly places within 5 km and
automatically widens to 10 km when fewer than five matches are found. MapLibre
renders OSM raster tiles; a Compose canvas fallback remains in place so the tab
still shows the origin and pins if the native map renderer fails on a device.

## Build from CLI

```
cd android
./gradlew assembleDebug
```

(The Gradle wrapper JAR is not checked in — run `gradle wrapper --gradle-version 8.9`
once from a machine with Gradle installed, or let Android Studio generate it on
first sync.)
