---
name: project-active-workspace
description: All current Sproutly work happens inside the `android/` native app — the web app at the repo root is frozen.
metadata:
  type: project
---

Sproutly's active workspace is the native Android client at `sproutly-app/android/`. The user is building it in Android Studio.

**Why:** The React/Vite web app at the repo root is the previous version; the user has switched to building the Android version as the primary product.

**How to apply:**
- Default new file paths to `android/app/src/main/java/com/sproutly/app/...` unless the user explicitly mentions the web app.
- Do not edit `src/`, `index.html`, `package.json`, `vite.config.ts` etc. without explicit instruction.
- The architecture, naming, and module layout to follow is in `android/README.md`.
- New skills like meal plan / nearby / scanner / AI features should land in the matching Kotlin module: [[project-sproutly-goal]].
