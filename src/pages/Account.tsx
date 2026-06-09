import { ChangeEvent, FormEvent, useEffect, useMemo, useState } from "react";
import {
  Camera,
  Check,
  ChevronDown,
  ImageUp,
  LogOut,
  Save,
  UserRound,
} from "lucide-react";
import { motion } from "framer-motion";
import { PageTitle, Reveal, Stagger, StaggerItem } from "../components/ui/Motion";
import { useAuth } from "../auth/AuthContext";
import {
  DIET_OPTIONS,
  DIET_TAG_OPTIONS,
  MAX_AVATAR_SIZE_BYTES,
  getAvatarUrl,
  loadProfile,
  saveProfile,
  uploadAvatar,
  type DietPreference,
  type DietTag,
  type Profile,
  type ProfileInput,
} from "../lib/profiles";
import {
  formatLocation,
  parseLocationInput,
  searchLocations,
  type LocationOption,
} from "../lib/locations";

type AccountForm = Omit<ProfileInput, "email">;

function userDefaultName(email: string | undefined) {
  return email?.split("@")[0] ?? "";
}

function normalizeDietPreference(value: string | null): DietPreference | "" {
  return DIET_OPTIONS.some((option) => option.value === value)
    ? (value as DietPreference)
    : "";
}

function normalizeDietTags(tags: string[]) {
  return tags.filter((tag): tag is DietTag =>
    DIET_TAG_OPTIONS.includes(tag as DietTag),
  );
}

function profileToForm(profile: Profile | null, email: string | undefined): AccountForm {
  return {
    displayName: profile?.display_name ?? userDefaultName(email),
    avatarPath: profile?.avatar_path ?? null,
    city: profile?.city ?? "",
    country: profile?.country ?? "",
    dietPreference: normalizeDietPreference(profile?.diet_preference ?? null),
    dietTags: normalizeDietTags(profile?.diet_tags ?? []),
  };
}

function formLocationValue(form: AccountForm) {
  if (form.city && form.country) return `${form.city}, ${form.country}`;
  return form.city || form.country;
}

export default function Account() {
  const { signOut, user } = useAuth();
  const [form, setForm] = useState<AccountForm>(() => profileToForm(null, user?.email));
  const [locationInput, setLocationInput] = useState(() => formLocationValue(form));
  const [locationFocused, setLocationFocused] = useState(false);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [avatarFile, setAvatarFile] = useState<File | null>(null);
  const [avatarPreview, setAvatarPreview] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [status, setStatus] = useState<string | null>(null);

  const avatarUrl = useMemo(
    () => avatarPreview ?? getAvatarUrl(form.avatarPath),
    [avatarPreview, form.avatarPath],
  );
  const locationSuggestions = useMemo(
    () => searchLocations(locationInput),
    [locationInput],
  );
  const showLocationSuggestions =
    locationFocused && locationInput.trim().length > 0 && locationSuggestions.length > 0;

  useEffect(() => {
    if (!user) return undefined;

    let mounted = true;
    setLoading(true);
    setError(null);

    loadProfile(user.id)
      .then((profile) => {
        if (!mounted) return;
        const nextForm = profileToForm(profile, user.email);
        setForm(nextForm);
        setLocationInput(formLocationValue(nextForm));
      })
      .catch((loadError) => {
        if (!mounted) return;
        setError(loadError instanceof Error ? loadError.message : "Could not load profile.");
      })
      .finally(() => {
        if (!mounted) return;
        setLoading(false);
      });

    return () => {
      mounted = false;
    };
  }, [user]);

  useEffect(() => {
    return () => {
      if (avatarPreview) URL.revokeObjectURL(avatarPreview);
    };
  }, [avatarPreview]);

  function updateField<Key extends keyof AccountForm>(
    key: Key,
    value: AccountForm[Key],
  ) {
    setForm((current) => ({ ...current, [key]: value }));
    setStatus(null);
  }

  function toggleTag(tag: DietTag) {
    setForm((current) => ({
      ...current,
      dietTags: current.dietTags.includes(tag)
        ? current.dietTags.filter((item) => item !== tag)
        : [...current.dietTags, tag],
    }));
    setStatus(null);
  }

  function updateLocationFromText(value: string) {
    const parsedLocation = parseLocationInput(value);
    setLocationInput(value);
    setForm((current) => ({
      ...current,
      city: parsedLocation.city,
      country: parsedLocation.country,
    }));
    setStatus(null);
  }

  function selectLocation(option: LocationOption) {
    setLocationInput(formatLocation(option));
    setForm((current) => ({
      ...current,
      city: option.city,
      country: option.country,
    }));
    setLocationFocused(false);
    setStatus(null);
  }

  function handleAvatarChange(event: ChangeEvent<HTMLInputElement>) {
    const file = event.target.files?.[0] ?? null;
    setError(null);
    setStatus(null);

    if (!file) {
      setAvatarFile(null);
      setAvatarPreview(null);
      return;
    }

    if (!file.type.startsWith("image/")) {
      setError("Choose an image file for your profile picture.");
      event.target.value = "";
      return;
    }

    if (file.size > MAX_AVATAR_SIZE_BYTES) {
      setError("Profile pictures must be 2 MB or smaller.");
      event.target.value = "";
      return;
    }

    if (avatarPreview) URL.revokeObjectURL(avatarPreview);
    setAvatarFile(file);
    setAvatarPreview(URL.createObjectURL(file));
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!user) return;

    setSaving(true);
    setError(null);
    setStatus(null);

    try {
      const avatarPath = avatarFile
        ? await uploadAvatar(user.id, avatarFile, form.avatarPath)
        : form.avatarPath;

      const savedProfile = await saveProfile(user.id, {
        ...form,
        email: user.email ?? null,
        avatarPath,
      });

      setForm(profileToForm(savedProfile, user.email));
      setLocationInput(formLocationValue(profileToForm(savedProfile, user.email)));
      setAvatarFile(null);
      setAvatarPreview(null);
      setStatus("Account settings saved.");
      window.dispatchEvent(new Event("sproutly-profile-updated"));
    } catch (saveError) {
      setError(saveError instanceof Error ? saveError.message : "Could not save profile.");
    } finally {
      setSaving(false);
    }
  }

  return (
    <form className="space-y-8" onSubmit={handleSubmit}>
      <PageTitle
        eyebrow="Account"
        title="Your Sproutly profile."
        body="Keep the essentials that shape meal ideas, product picks, and nearby plant-based options."
      />

      <Reveal mode="scale">
        <section className="grid gap-5 rounded-[1.5rem] border border-line/70 bg-panel-soft/60 p-4">
          <div className="flex items-center gap-4">
            <div className="relative h-24 w-24 shrink-0 overflow-hidden rounded-[1.5rem] border border-line/70 bg-void/62">
              {avatarUrl ? (
                <img
                  src={avatarUrl}
                  alt=""
                  className="h-full w-full object-cover"
                />
              ) : (
                <div className="grid h-full w-full place-items-center text-leaf">
                  <UserRound className="h-9 w-9" />
                </div>
              )}
              <span className="absolute bottom-2 right-2 grid h-8 w-8 place-items-center rounded-full bg-leaf text-void ring-2 ring-void">
                <Camera className="h-4 w-4" />
              </span>
            </div>

            <div className="min-w-0 flex-1">
              <p className="truncate text-sm font-black text-ink">
                {form.displayName || userDefaultName(user?.email)}
              </p>
              <p className="mt-1 truncate text-xs font-bold text-charcoal">
                {user?.email}
              </p>
              <label className="mt-4 inline-flex min-h-11 cursor-pointer items-center justify-center gap-2 rounded-2xl border border-leaf/35 bg-leaf/12 px-4 py-3 text-xs font-black uppercase tracking-[0.14em] text-mint transition hover:bg-leaf/18">
                <ImageUp className="h-4 w-4" />
                Upload
                <input
                  type="file"
                  accept="image/*"
                  onChange={handleAvatarChange}
                  className="sr-only"
                />
              </label>
            </div>
          </div>

          {error ? (
            <div className="rounded-2xl border border-red-300/30 bg-red-400/10 p-3 text-sm font-bold leading-5 text-red-100">
              {error}
            </div>
          ) : null}
          {status ? (
            <div className="rounded-2xl border border-leaf/30 bg-leaf/10 p-3 text-sm font-bold leading-5 text-mint">
              {status}
            </div>
          ) : null}
        </section>
      </Reveal>

      {loading ? (
        <div className="grid gap-3">
          {Array.from({ length: 3 }, (_item, index) => (
            <div
              key={index}
              className="h-32 animate-pulse rounded-[1.5rem] border border-line/60 bg-panel-soft/45"
            />
          ))}
        </div>
      ) : (
        <Stagger className="grid gap-4">
          <StaggerItem mode="rise" className="relative z-40">
            <section className="relative z-40 grid gap-4 rounded-[1.5rem] border border-line/70 bg-panel-soft/58 p-4 shadow-[0_18px_70px_-50px_black]">
              <h2 className="text-lg font-black text-ink">Basics</h2>
              <label className="grid gap-2 text-xs font-black uppercase tracking-[0.16em] text-charcoal">
                Display name
                <input
                  value={form.displayName}
                  onChange={(event) => updateField("displayName", event.target.value)}
                  className="rounded-2xl border border-line/70 bg-void/45 px-4 py-3 text-sm font-bold normal-case tracking-normal text-ink outline-none transition placeholder:text-charcoal/45 focus:border-leaf/60 focus:ring-2 focus:ring-leaf/20"
                  placeholder="Name"
                />
              </label>
              <label className="relative grid gap-2 text-xs font-black uppercase tracking-[0.16em] text-charcoal">
                Location
                <input
                  value={locationInput}
                  onChange={(event) => updateLocationFromText(event.target.value)}
                  onFocus={() => setLocationFocused(true)}
                  onBlur={() => {
                    window.setTimeout(() => setLocationFocused(false), 120);
                  }}
                  onKeyDown={(event) => {
                    if (event.key === "Escape") setLocationFocused(false);
                    if (event.key === "Enter" && showLocationSuggestions) {
                      event.preventDefault();
                      selectLocation(locationSuggestions[0]);
                    }
                  }}
                  role="combobox"
                  aria-expanded={showLocationSuggestions}
                  aria-controls="account-location-options"
                  autoComplete="off"
                  className="rounded-2xl border border-line/70 bg-void/45 px-4 py-3 pr-10 text-sm font-bold normal-case tracking-normal text-ink outline-none transition placeholder:text-charcoal/45 focus:border-leaf/60 focus:ring-2 focus:ring-leaf/20"
                  placeholder="Madrid, Spain"
                />
                <ChevronDown className="pointer-events-none absolute bottom-3.5 right-4 h-4 w-4 text-charcoal" />
                {showLocationSuggestions ? (
                  <div
                    id="account-location-options"
                    role="listbox"
                    className="absolute left-0 right-0 top-full z-[100] mt-2 max-h-64 overflow-y-auto rounded-2xl border border-line/70 bg-void/95 p-1 shadow-[0_20px_70px_-40px_black] backdrop-blur-xl"
                  >
                    {locationSuggestions.map((option) => (
                      <button
                        key={formatLocation(option)}
                        type="button"
                        role="option"
                        onMouseDown={(event) => event.preventDefault()}
                        onClick={() => selectLocation(option)}
                        className="flex w-full items-center justify-between gap-3 rounded-xl px-3 py-3 text-left transition hover:bg-leaf/12 focus:bg-leaf/12 focus:outline-none"
                      >
                        <span className="min-w-0 truncate text-sm font-black normal-case tracking-normal text-ink">
                          {option.city}
                        </span>
                        <span className="shrink-0 text-xs font-bold normal-case tracking-normal text-charcoal">
                          {option.country}
                        </span>
                      </button>
                    ))}
                  </div>
                ) : null}
              </label>
            </section>
          </StaggerItem>

          <StaggerItem mode="drift" className="relative z-10">
            <section className="grid gap-4 rounded-[1.5rem] border border-line/70 bg-panel-soft/58 p-4 shadow-[0_18px_70px_-50px_black]">
              <h2 className="text-lg font-black text-ink">Plant-based diet</h2>
              <label className="relative grid gap-2 text-xs font-black uppercase tracking-[0.16em] text-charcoal">
                Primary preference
                <select
                  value={form.dietPreference}
                  onChange={(event) =>
                    updateField(
                      "dietPreference",
                      event.target.value as AccountForm["dietPreference"],
                    )
                  }
                  className="appearance-none rounded-2xl border border-line/70 bg-void/45 px-4 py-3 pr-10 text-sm font-bold normal-case tracking-normal text-ink outline-none transition focus:border-leaf/60 focus:ring-2 focus:ring-leaf/20"
                >
                  <option value="">Choose diet</option>
                  {DIET_OPTIONS.map((option) => (
                    <option key={option.value} value={option.value}>
                      {option.label}
                    </option>
                  ))}
                </select>
                <ChevronDown className="pointer-events-none absolute bottom-3.5 right-4 h-4 w-4 text-charcoal" />
              </label>

              <div className="grid gap-3">
                <p className="text-xs font-black uppercase tracking-[0.16em] text-charcoal">
                  Preferences
                </p>
                <div className="flex flex-wrap gap-2">
                  {DIET_TAG_OPTIONS.map((tag) => {
                    const selected = form.dietTags.includes(tag);
                    return (
                      <button
                        key={tag}
                        type="button"
                        onClick={() => toggleTag(tag)}
                        className={`inline-flex min-h-10 items-center gap-2 rounded-full border px-3 py-2 text-xs font-black transition ${
                          selected
                            ? "border-leaf/60 bg-leaf text-void"
                            : "border-line/70 bg-void/45 text-charcoal hover:border-leaf/45 hover:text-mint"
                        }`}
                      >
                        {selected ? <Check className="h-3.5 w-3.5" /> : null}
                        {tag}
                      </button>
                    );
                  })}
                </div>
              </div>
            </section>
          </StaggerItem>

          <StaggerItem mode="scale" className="relative z-0">
            <section className="grid gap-3 rounded-[1.5rem] border border-line/70 bg-panel-soft/58 p-4 shadow-[0_18px_70px_-50px_black]">
              <motion.button
                type="submit"
                disabled={saving}
                whileHover={{ y: saving ? 0 : -2 }}
                whileTap={{ scale: saving ? 1 : 0.98 }}
                className="flex min-h-14 items-center justify-center gap-2 rounded-2xl border border-leaf/30 bg-leaf px-4 py-3 text-sm font-black uppercase tracking-[0.16em] text-void transition hover:bg-mint disabled:cursor-not-allowed disabled:opacity-50"
              >
                <Save className="h-4 w-4" />
                {saving ? "Saving" : "Save settings"}
              </motion.button>
              <button
                type="button"
                onClick={() => void signOut()}
                className="flex min-h-14 items-center justify-center gap-2 rounded-2xl border border-leaf/35 bg-leaf/12 px-4 py-3 text-sm font-black uppercase tracking-[0.16em] text-mint transition hover:bg-leaf/18"
              >
                <LogOut className="h-4 w-4" />
                Sign out
              </button>
            </section>
          </StaggerItem>
        </Stagger>
      )}
    </form>
  );
}
