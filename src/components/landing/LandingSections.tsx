// LandingSections.tsx
// ----------------------------------------------------------------------------
// Everything that lives BELOW the animated hero on the landing page.
// Pure content — no scroll hijacking. Sections use IDs that match the
// top-bar anchor links (#about, #pillars, #how, #cta).

import { motion, type Variants } from "framer-motion";
import { Link } from "react-router-dom";
import {
  Home as HomeIcon,
  ShoppingBag,
  MapPin,
  ChefHat,
  Sparkles,
  Heart,
  Leaf,
  Globe,
  ScanLine,
  CalendarRange,
  ShieldCheck,
  ArrowRight,
} from "lucide-react";

// Reveal-on-scroll variants reused across blocks for a cohesive feel.
const fadeUp: Variants = {
  hidden: { opacity: 0, y: 24 },
  show: { opacity: 1, y: 0, transition: { duration: 0.6, ease: [0.7, 0, 0.2, 1] } },
};

// Helper: wraps children in a viewport-triggered reveal.
function Reveal({
  children,
  delay = 0,
  className,
}: {
  children: React.ReactNode;
  delay?: number;
  className?: string;
}) {
  return (
    <motion.div
      variants={fadeUp}
      initial="hidden"
      whileInView="show"
      viewport={{ once: true, margin: "-80px" }}
      transition={{ delay }}
      className={className}
    >
      {children}
    </motion.div>
  );
}

// === Data for the feature/pillar grid ===
const PILLAR_FEATURES = [
  {
    icon: HomeIcon,
    title: "Daily dashboard",
    body: "Reminders, what to cook today, and the highlights worth your attention — all in one calm screen.",
    tone: "leaf",
    to: "/home",
  },
  {
    icon: ShoppingBag,
    title: "Plant-based products",
    body: "Curated groceries, weekly deals, and ingredient labels you can actually trust.",
    tone: "clay",
    to: "/products",
  },
  {
    icon: MapPin,
    title: "Nearby spots",
    body: "Find vegan-friendly restaurants and supermarkets around you, vetted by the community.",
    tone: "sage",
    to: "/map",
  },
  {
    icon: ChefHat,
    title: "Recipes & meal plans",
    body: "Plant-based recipes plus AI-assisted weekly plans tailored to your pantry and goals.",
    tone: "leaf",
    to: "/recipes",
  },
] as const;

// === Steps for "how it works" ===
const STEPS = [
  {
    icon: Sparkles,
    title: "Tell us what you eat",
    body: "Vegan, vegetarian, or just curious — Sproutly adapts to where you are on the journey.",
  },
  {
    icon: ScanLine,
    title: "Scan, browse, discover",
    body: "Scan a label, browse curated products, or peek at what's good near you right now.",
  },
  {
    icon: CalendarRange,
    title: "Plan your week",
    body: "Get a meal plan, a shopping list, and recipes — generated in seconds, swappable anytime.",
  },
];

// === Stats / why plant-based ===
const STATS = [
  { icon: Leaf, value: "−73%", label: "less land use vs. omnivore diet" },
  { icon: Globe, value: "−50%", label: "lower food-related emissions" },
  { icon: Heart, value: "+9 yrs", label: "average added healthspan*" },
  { icon: ShieldCheck, value: "100%", label: "plant-based, always vetted" },
];

// === Who it's for ===
const AUDIENCES = [
  {
    tag: "Vegans",
    title: "Skip the label-reading headache",
    body: "Trustworthy product info, vegan-only filters, and shops you can actually visit.",
  },
  {
    tag: "Vegetarians",
    title: "Eat more variety, less guesswork",
    body: "Discover new ingredients, swap meals you're bored of, and stay on top of protein and iron.",
  },
  {
    tag: "Plant-curious",
    title: "Try without committing",
    body: "Start with one meal a day. Sproutly helps you find dishes you'll genuinely love.",
  },
];

export default function LandingSections() {
  return (
    <div className="bg-cream text-forest">
      {/* === ABOUT ============================================================ */}
      <section id="about" className="mx-auto max-w-5xl px-5 py-20 sm:py-28">
        <Reveal>
          <div className="text-[0.7rem] font-semibold uppercase tracking-[0.3em] text-leaf">
            What is Sproutly
          </div>
        </Reveal>
        <Reveal delay={0.05}>
          <h2 className="mt-3 font-display text-3xl font-extrabold leading-tight sm:text-5xl">
            The all-in-one hub for{" "}
            <span className="text-leaf">plant-based living</span>.
          </h2>
        </Reveal>
        <Reveal delay={0.1}>
          <p className="mt-5 max-w-2xl text-base text-forest/75 sm:text-lg">
            Sproutly brings together everything a plant-based eater juggles
            today — what to cook, where to shop, what to buy, and where to eat —
            into one practical, trustworthy app. No more jumping between five
            tabs to plan a Tuesday.
          </p>
        </Reveal>
      </section>

      {/* === PILLARS ========================================================== */}
      <section
        id="pillars"
        className="border-y border-forest/10 bg-white py-20 sm:py-28"
      >
        <div className="mx-auto max-w-5xl px-5">
          <Reveal>
            <div className="text-[0.7rem] font-semibold uppercase tracking-[0.3em] text-leaf">
              Four pillars, one app
            </div>
          </Reveal>
          <Reveal delay={0.05}>
            <h2 className="mt-3 font-display text-3xl font-extrabold sm:text-4xl">
              Built around how you actually eat.
            </h2>
          </Reveal>

          <div className="mt-10 grid grid-cols-1 gap-4 sm:grid-cols-2">
            {PILLAR_FEATURES.map(({ icon: Icon, title, body, tone, to }, i) => {
              const toneBg = {
                leaf: "bg-leaf/10",
                clay: "bg-clay/15",
                sage: "bg-sage/30",
              }[tone];
              const toneIcon = {
                leaf: "bg-leaf text-cream",
                clay: "bg-clay text-cream",
                sage: "bg-forest text-cream",
              }[tone];
              return (
                <Reveal key={title} delay={i * 0.08}>
                  <Link
                    to={to}
                    className={`group flex h-full flex-col rounded-3xl ${toneBg} p-6 ring-1 ring-black/5 transition hover:-translate-y-0.5 hover:shadow-lg`}
                  >
                    <div
                      className={`grid h-12 w-12 place-items-center rounded-2xl ${toneIcon}`}
                    >
                      <Icon className="h-6 w-6" />
                    </div>
                    <h3 className="mt-5 font-display text-xl font-extrabold">
                      {title}
                    </h3>
                    <p className="mt-2 text-sm text-forest/75">{body}</p>
                    <div className="mt-4 inline-flex items-center gap-1 text-xs font-semibold uppercase tracking-wider text-leaf">
                      Explore
                      <ArrowRight className="h-3.5 w-3.5 transition group-hover:translate-x-0.5" />
                    </div>
                  </Link>
                </Reveal>
              );
            })}
          </div>
        </div>
      </section>

      {/* === HOW IT WORKS ===================================================== */}
      <section id="how" className="mx-auto max-w-5xl px-5 py-20 sm:py-28">
        <Reveal>
          <div className="text-[0.7rem] font-semibold uppercase tracking-[0.3em] text-leaf">
            How it works
          </div>
        </Reveal>
        <Reveal delay={0.05}>
          <h2 className="mt-3 font-display text-3xl font-extrabold sm:text-4xl">
            Three steps from open to dinner.
          </h2>
        </Reveal>

        <ol className="mt-10 grid grid-cols-1 gap-6 sm:grid-cols-3">
          {STEPS.map(({ icon: Icon, title, body }, i) => (
            <Reveal key={title} delay={i * 0.1}>
              <li className="relative flex h-full flex-col rounded-3xl bg-white p-6 ring-1 ring-black/5">
                <span className="absolute right-5 top-5 font-display text-4xl font-extrabold text-forest/10">
                  0{i + 1}
                </span>
                <div className="grid h-12 w-12 place-items-center rounded-2xl bg-forest text-cream">
                  <Icon className="h-6 w-6" />
                </div>
                <h3 className="mt-5 font-display text-lg font-extrabold">
                  {title}
                </h3>
                <p className="mt-2 text-sm text-forest/75">{body}</p>
              </li>
            </Reveal>
          ))}
        </ol>
      </section>

      {/* === WHY PLANT-BASED (stats) ========================================== */}
      <section className="relative overflow-hidden bg-forest py-20 text-cream sm:py-28">
        {/* Soft decorative blobs — pure CSS, no images required. */}
        <div className="pointer-events-none absolute -left-20 -top-20 h-72 w-72 rounded-full bg-leaf/30 blur-3xl" />
        <div className="pointer-events-none absolute -bottom-24 -right-10 h-80 w-80 rounded-full bg-clay/30 blur-3xl" />

        <div className="relative mx-auto max-w-5xl px-5">
          <Reveal>
            <div className="text-[0.7rem] font-semibold uppercase tracking-[0.3em] text-sage">
              Why plant-based
            </div>
          </Reveal>
          <Reveal delay={0.05}>
            <h2 className="mt-3 max-w-2xl font-display text-3xl font-extrabold sm:text-4xl">
              Better for you. Better for the planet. Easier than you think.
            </h2>
          </Reveal>

          <div className="mt-12 grid grid-cols-2 gap-4 sm:grid-cols-4">
            {STATS.map(({ icon: Icon, value, label }, i) => (
              <Reveal key={label} delay={i * 0.08}>
                <div className="rounded-3xl bg-white/5 p-5 ring-1 ring-white/10 backdrop-blur-sm">
                  <Icon className="h-6 w-6 text-sage" />
                  <div className="mt-4 font-display text-3xl font-extrabold sm:text-4xl">
                    {value}
                  </div>
                  <div className="mt-1 text-xs leading-snug text-cream/75">
                    {label}
                  </div>
                </div>
              </Reveal>
            ))}
          </div>
          <Reveal delay={0.4}>
            <p className="mt-6 text-[0.7rem] text-cream/50">
              * Indicative figures from peer-reviewed sustainability and
              nutrition studies. Your mileage may vary.
            </p>
          </Reveal>
        </div>
      </section>

      {/* === WHO IT'S FOR ===================================================== */}
      <section className="bg-white py-20 sm:py-28">
        <div className="mx-auto max-w-5xl px-5">
          <Reveal>
            <div className="text-[0.7rem] font-semibold uppercase tracking-[0.3em] text-leaf">
              Built for you
            </div>
          </Reveal>
          <Reveal delay={0.05}>
            <h2 className="mt-3 font-display text-3xl font-extrabold sm:text-4xl">
              Whether you're all-in or just exploring.
            </h2>
          </Reveal>

          <div className="mt-10 grid grid-cols-1 gap-4 sm:grid-cols-3">
            {AUDIENCES.map(({ tag, title, body }, i) => (
              <Reveal key={tag} delay={i * 0.08}>
                <div className="flex h-full flex-col rounded-3xl border border-forest/10 p-6">
                  <span className="self-start rounded-full bg-leaf/15 px-3 py-1 text-[0.65rem] font-semibold uppercase tracking-wider text-leaf">
                    {tag}
                  </span>
                  <h3 className="mt-4 font-display text-lg font-extrabold">
                    {title}
                  </h3>
                  <p className="mt-2 text-sm text-forest/75">{body}</p>
                </div>
              </Reveal>
            ))}
          </div>
        </div>
      </section>

      {/* === CTA ============================================================== */}
      <section id="cta" className="bg-cream py-20 sm:py-28">
        <div className="mx-auto max-w-3xl px-5 text-center">
          <Reveal>
            <Leaf className="mx-auto h-10 w-10 text-leaf" />
          </Reveal>
          <Reveal delay={0.05}>
            <h2 className="mt-5 font-display text-4xl font-extrabold leading-tight sm:text-5xl">
              Start eating plants, the easy way.
            </h2>
          </Reveal>
          <Reveal delay={0.1}>
            <p className="mx-auto mt-4 max-w-xl text-forest/70 sm:text-lg">
              Free to try. No credit card. Designed for your phone — works
              great on the web too.
            </p>
          </Reveal>
          <Reveal delay={0.15}>
            <div className="mt-8 flex flex-col items-center justify-center gap-3 sm:flex-row">
              <Link
                to="/home"
                className="inline-flex items-center gap-2 rounded-full bg-forest px-6 py-3 text-sm font-semibold text-cream transition hover:bg-leaf"
              >
                Open Sproutly
                <ArrowRight className="h-4 w-4" />
              </Link>
              <a
                href="#about"
                className="inline-flex items-center gap-2 rounded-full border border-forest/20 px-6 py-3 text-sm font-semibold text-forest transition hover:bg-forest/5"
              >
                Learn more
              </a>
            </div>
          </Reveal>
        </div>
      </section>

      {/* === FOOTER =========================================================== */}
      <footer className="border-t border-forest/10 bg-cream">
        <div className="mx-auto flex max-w-5xl flex-col items-center justify-between gap-3 px-5 py-8 text-xs text-forest/60 sm:flex-row">
          <div className="flex items-center gap-2 font-display text-base font-extrabold text-forest">
            <Leaf className="h-4 w-4 text-leaf" />
            Sproutly
          </div>
          <div>© {new Date().getFullYear()} Sproutly. Plants only.</div>
          <div className="flex gap-4">
            <a href="#about">About</a>
            <a href="#pillars">Features</a>
            <a href="#cta">Get started</a>
          </div>
        </div>
      </footer>
    </div>
  );
}
