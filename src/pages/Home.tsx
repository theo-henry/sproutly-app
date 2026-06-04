import SectionHeader from "../components/ui/SectionHeader";
import PlaceholderCard from "../components/ui/PlaceholderCard";
import { PageTitle, Reveal, Stagger, StaggerItem } from "../components/ui/Motion";

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
      <PageTitle
        eyebrow="Home"
        title="Plant-based decisions without friction."
        body="A daily command center for meals, reminders, nearby options, and product signals."
      />

      <section className="grid gap-4 lg:grid-cols-[1.2fr_0.8fr]">
        <Reveal mode="scale">
          <div className="dark-grid relative min-h-72 overflow-hidden rounded-[1.5rem] border border-line/70 bg-void p-6">
            <div className="absolute right-6 top-6 h-20 w-20 rounded-full border border-leaf/20" />
            <div className="absolute right-12 top-12 h-8 w-8 rounded-full bg-leaf/18 blur-md" />
            <div className="relative max-w-xl">
              <p className="text-xs font-black uppercase tracking-[0.24em] text-leaf/80">
                Today
              </p>
              <h2 className="mt-4 text-3xl font-black leading-tight text-ink sm:text-5xl">
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
        <Stagger className="grid grid-cols-1 gap-3 sm:grid-cols-3">
          {ACTIONS.map((action, index) => (
            <StaggerItem key={action.title} mode={index === 1 ? "scale" : "rise"}>
              <PlaceholderCard {...action} />
            </StaggerItem>
          ))}
        </Stagger>
      </section>

      <section>
        <SectionHeader title="For you" href="/recipes" />
        <Stagger className="grid gap-3 sm:grid-cols-2">
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
