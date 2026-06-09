# Sproutly

## Supabase setup

Create a local `.env.local` file from `.env.example` and fill in:

```bash
VITE_SUPABASE_URL=https://your-project-ref.supabase.co
VITE_SUPABASE_PUBLISHABLE_KEY=your-publishable-or-anon-key
VITE_SUPABASE_DEMO_EMAIL=demo@example.com
VITE_SUPABASE_DEMO_PASSWORD=your-demo-password
```

Do not commit `.env.local`. The repo ignores `.env` and `.env.*`, while `.env.example`
contains only placeholders.

Find the Supabase URL and publishable key in the Supabase dashboard under the
project's API/connect settings. Create the demo account as a normal
email/password user in Supabase Auth, then put that account's credentials in
`.env.local`.

Run the SQL in `supabase/migrations/20260609_auth_meal_plans.sql` in the
Supabase SQL editor to create the per-user meal-plan table and RLS policies.
l
