# Sproutly Project Brief

## Goal

Build Sproutly as an all-in-one hub for people following plant-based diets, including vegetarians, vegans, and people trying to eat more plant-based food.

The website should centralize the daily workflows a plant-based user needs:

- Home tab: a clear dashboard for discovery, reminders, quick actions, and relevant highlights.
- Products tab: plant-based products, deals, offers, and useful shopping opportunities.
- Local map tab: supermarkets and restaurants near the user that are fully plant-based or provide plant-based options.
- Recipes and meal planning tab: recipes plus AI-assisted meal plan creation.

The product should feel practical, trustworthy, and easy to use. It should help users decide what to buy, where to eat or shop, and what to cook without needing to jump between disconnected apps.

## Active Workspace

**All current development happens inside `android/`** — the native Kotlin + Jetpack Compose Android app. The user builds it in Android Studio. The React/Vite web app at the repo root is the prior version and is frozen; do not edit `src/`, `index.html`, `package.json`, `vite.config.ts`, `supabase/`, etc. unless explicitly asked.

Default any new code, refactors, and feature work to `android/app/src/main/java/com/sproutly/app/...`. The module layout and dependency choices are documented in `android/README.md`.

## Project Memory Protocol

From now on, whenever the user corrects the assistant or says to remember something about this project, save it as its own `.md` file inside a `memory/` folder at the root of this workspace.

Prefix each memory file with one of:

- `user_` for information about how the user personally works.
- `project_` for information about this specific project.
- `feedback_` for corrections to assistant behavior.
- `reference_` for links, facts, or external context to remember.

Keep `memory/MEMORY.md` as an index of every rule with a one-line summary, so the right memory loads next session.

Maintain two companion files:

- `memory/lessons.md`: a narrative log of strategic learnings. When the user says something is a "lesson" or a "pattern we should remember," or when the same kind of correction repeats, append a new entry with what happened, why it was wrong, what changed, and the deeper principle.
- `tasks/todo.md`: the active sprint todo list. Plan here before building and mark items complete as they ship.

At the start of every new session, read:

- `memory/MEMORY.md`
- `memory/lessons.md`
- `tasks/todo.md`

Then continue working as normal.

Before writing code or project files, also read `CLAUDE.md` and the files in
`memory/`. If those files are not up to date with the user's current project
instructions, update them before continuing with implementation work.
