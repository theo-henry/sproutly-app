// Sproutly's four pillars — the landing slider cycles through them.
// Each pillar = one paginated panel on the landing page.

export type Pillar = {
  /** Internal id used as React key. */
  id: string;
  /** Big display word, e.g. "DISCOVER". */
  name: string;
  /** Where to split the word so left/right halves can animate apart. */
  splitAt: number;
  /** Sub-label rendered above the title. */
  kicker: string;
  /** 1–2 line description shown in the bottom-left info card. */
  blurb: string;
  /** Background color for this panel (animated body transition). */
  bg: string;
  /** Foreground text color for this panel. */
  fg: string;
  /** Hero image URL — square, centered subject, transparent or full-bleed. */
  image: string;
  /** Where this pillar deep-links into the app. */
  route: string;
  /** CTA copy on the info card. */
  cta: string;
};

export const PILLARS: Pillar[] = [
  {
    id: "discover",
    name: "DISCOVER",
    splitAt: 3, // "DIS" … "COVER"
    kicker: "Your daily plant-based hub",
    blurb:
      "A personal dashboard for reminders, fresh ideas, and what to eat today — all in one place.",
    bg: "oklch(0.72 0.13 145)", // leafy green
    fg: "oklch(0.98 0.02 95)",
    image:
      "https://images.unsplash.com/photo-1502741338009-cac2772e18bc?w=900&h=900&fit=crop&auto=format",
    route: "/home",
    cta: "Open dashboard",
  },
  {
    id: "products",
    name: "PRODUCTS",
    splitAt: 3, // "PRO" … "DUCTS"
    kicker: "Shop plant-based, smarter",
    blurb:
      "Curated plant-based products, weekly deals, and labels you can actually trust.",
    bg: "oklch(0.78 0.14 75)", // golden olive
    fg: "oklch(0.25 0.05 60)",
    image:
      "https://images.unsplash.com/photo-1542838132-92c53300491e?w=900&h=900&fit=crop&auto=format",
    route: "/products",
    cta: "Browse products",
  },
  {
    id: "nearby",
    name: "NEARBY",
    splitAt: 3, // "NEA" … "RBY"
    kicker: "Plant-based on the map",
    blurb:
      "Find vegan-friendly restaurants and supermarkets around you, vetted by the community.",
    bg: "oklch(0.62 0.14 195)", // sea-green teal
    fg: "oklch(0.98 0.02 95)",
    image:
      "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?w=900&h=900&fit=crop&auto=format",
    route: "/map",
    cta: "Open map",
  },
  {
    id: "recipes",
    name: "RECIPES",
    splitAt: 3, // "REC" … "IPES"
    kicker: "Cook with confidence",
    blurb:
      "Plant-based recipes plus AI-assisted meal plans tailored to your week and pantry.",
    bg: "oklch(0.68 0.16 40)", // terracotta clay
    fg: "oklch(0.98 0.02 95)",
    image:
      "https://images.unsplash.com/photo-1543353071-10c8ba85a904?w=900&h=900&fit=crop&auto=format",
    route: "/recipes",
    cta: "Find recipes",
  },
];
