create table if not exists public.profiles (
  id uuid primary key references auth.users(id) on delete cascade,
  email text,
  display_name text,
  is_demo boolean not null default false,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create table if not exists public.meal_plans (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references auth.users(id) on delete cascade,
  week_start date not null,
  days jsonb not null default '[]'::jsonb,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  unique (user_id, week_start)
);

create unique index if not exists profiles_email_unique_idx
on public.profiles (lower(email))
where email is not null;

create index if not exists meal_plans_user_id_idx
on public.meal_plans using btree (user_id);

create index if not exists meal_plans_user_week_idx
on public.meal_plans using btree (user_id, week_start desc);

create or replace function public.set_updated_at()
returns trigger
language plpgsql
set search_path = public
as $$
begin
  new.updated_at = now();
  return new;
end;
$$;

create or replace function public.handle_new_user()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
begin
  insert into public.profiles (id, email, display_name, is_demo)
  values (
    new.id,
    new.email,
    coalesce(new.raw_user_meta_data ->> 'display_name', split_part(new.email, '@', 1)),
    new.email = 'demo@sproutly.com'
  )
  on conflict (id) do update
  set
    email = excluded.email,
    updated_at = now();

  return new;
end;
$$;

drop trigger if exists profiles_set_updated_at on public.profiles;
create trigger profiles_set_updated_at
before update on public.profiles
for each row
execute function public.set_updated_at();

drop trigger if exists meal_plans_set_updated_at on public.meal_plans;
create trigger meal_plans_set_updated_at
before update on public.meal_plans
for each row
execute function public.set_updated_at();

drop trigger if exists on_auth_user_created on auth.users;
create trigger on_auth_user_created
after insert on auth.users
for each row
execute function public.handle_new_user();

alter table public.profiles enable row level security;
alter table public.meal_plans enable row level security;

drop policy if exists "Users can view their own profile" on public.profiles;
create policy "Users can view their own profile"
on public.profiles for select
to authenticated
using ((select auth.uid()) = id);

drop policy if exists "Users can update their own profile" on public.profiles;
create policy "Users can update their own profile"
on public.profiles for update
to authenticated
using ((select auth.uid()) = id)
with check ((select auth.uid()) = id);

drop policy if exists "Users can view their own meal plans" on public.meal_plans;
create policy "Users can view their own meal plans"
on public.meal_plans for select
to authenticated
using ((select auth.uid()) = user_id);

drop policy if exists "Users can create their own meal plans" on public.meal_plans;
create policy "Users can create their own meal plans"
on public.meal_plans for insert
to authenticated
with check ((select auth.uid()) = user_id);

drop policy if exists "Users can update their own meal plans" on public.meal_plans;
create policy "Users can update their own meal plans"
on public.meal_plans for update
to authenticated
using ((select auth.uid()) = user_id)
with check ((select auth.uid()) = user_id);

drop policy if exists "Users can delete their own meal plans" on public.meal_plans;
create policy "Users can delete their own meal plans"
on public.meal_plans for delete
to authenticated
using ((select auth.uid()) = user_id);
