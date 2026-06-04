import { lazy, Suspense } from "react";
import SectionHeader from "../components/ui/SectionHeader";
import PlaceholderCard from "../components/ui/PlaceholderCard";
import { Reveal, Stagger, StaggerItem } from "../components/ui/Motion";

const PlantHero = lazy(() => import("../components/home/PlantHero"));

const ACTIONS = [
  { title: "Log meal", subtitle: "Add breakfast or snack", tone: "sage" as const },
  { title: "Scan label", subtitle: "Check ingredients fast", tone: "mint" as const },
  { title: "Find spot", subtitle: "Nearby plant options", tone: "moss" as const },
];

// Home dashboard — discovery, reminders, quick actions, highlights.
// Skeleton content lives here until real data wiring lands.
export default function Home() {
  return (
    <div className="space-y-8">
      <section className="relative -mx-4 -mt-4 h-[78vh] min-h-[460px] overflow-hidden rounded-b-[2rem] border-b border-line/40 bg-black">
        <Suspense fallback={<div className="absolute inset-0 bg-black" />}>
          <PlantHero className="absolute inset-0" />
        </Suspense>
        {/* Dark overlay — keeps text legible while preserving the plant's rim light. */}
        <div className="pointer-events-none absolute inset-0 bg-[radial-gradient(ellipse_at_center,_transparent_30%,_rgba(0,0,0,0.55)_75%)]" />
        <div className="pointer-events-none absolute inset-x-0 top-0 h-1/2 bg-gradient-to-b from-black/55 to-transparent" />
        <div className="pointer-events-none absolute inset-x-0 bottom-0 h-1/3 bg-gradient-to-t from-black/70 to-transparent" />

        <div className="relative z-10 flex h-full flex-col justify-between p-6">
          <div className="max-w-md">
            <p className="text-xs font-black uppercase tracking-[0.32em] text-leaf/80">
              Sproutly
            </p>
            <h1 className="mt-4 text-4xl font-black leading-[1.05] text-ink">
              Plant-based decisions without friction.
            </h1>
          </div>
          <p className="max-w-sm text-sm leading-6 text-charcoal/90">
            A daily command center for meals, reminders, nearby options, and product signals.
          </p>
        </div>
      </section>

      <section className="grid gap-4">
        <Reveal mode="scale">
          <div className="dark-grid relative min-h-72 overflow-hidden rounded-[1.5rem] border border-line/70 bg-void p-6">
            <div className="absolute right-6 top-6 h-20 w-20 rounded-full border border-leaf/20" />
            <div className="absolute right-12 top-12 h-8 w-8 rounded-full bg-leaf/18 blur-md" />
            <div className="relative max-w-xl">
              <p className="text-xs font-black uppercase tracking-[0.24em] text-leaf/80">
                Today
              </p>
              <h2 className="mt-4 text-3xl font-black leading-tight text-ink">
                Tofu scramble, lentil bowl, cashew greens.
              </h2>
              <p className="mt-4 max-w-md text-sm leading-6 text-charcoal">
                Balanced for protein, iron, and prep time. Swap anything before
                generating the shopping list.
              </p>
            </div>
            <div className="relative mt-8 grid grid-cols-3 gap-2 text-center">
              {["28g protein", "32 min", "€9.40"].map((value) => (
                <div key={value} className="rounded-2xl border border-line/60 bg-panel/75 p-3 text-xs font-black text-mint">
                  {value}
                </div>
              ))}
            </div>
          </div>
        </Reveal>

        <Stagger className="grid gap-4">
          <StaggerItem mode="drift">
            <PlaceholderCard
              tone="leaf"
              meta="Reminder"
              title="Hydration and B12"
              subtitle="Drink 2L of water · Take supplement after lunch"
            />
          </StaggerItem>
          <StaggerItem mode="scale">
            <PlaceholderCard
              tone="moss"
              meta="Nearby"
              title="Three plant-forward spots"
              subtitle="Open now within 1.2 km"
            />
          </StaggerItem>
        </Stagger>
      </section>

      <section>
        <SectionHeader title="Quick actions" />
        <Stagger className="grid grid-cols-1 gap-3">
          {ACTIONS.map((action, index) => (
            <StaggerItem key={action.title} mode={index === 1 ? "scale" : "rise"}>
              <PlaceholderCard {...action} />
            </StaggerItem>
          ))}
        </Stagger>
      </section>

      <section>
        <SectionHeader title="For you" href="/recipes" />
        <Stagger className="grid gap-3">
          <StaggerItem mode="drift">
            <PlaceholderCard
              title="High-protein chickpea bowl"
              subtitle="25 min · 480 kcal · 28g protein"
              tone="leaf"
            />
          </StaggerItem>
          <StaggerItem mode="rise">
            <PlaceholderCard
              title="Creamy cashew pasta"
              subtitle="20 min · 520 kcal · 18g protein"
              tone="mint"
            />
          </StaggerItem>
        </Stagger>
      </section>
    </div>
  );
}
