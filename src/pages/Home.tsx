import SectionHeader from "../components/ui/SectionHeader";
import PlaceholderCard from "../components/ui/PlaceholderCard";

// Home dashboard — discovery, reminders, quick actions, highlights.
// Skeleton content lives here until real data wiring lands.
export default function Home() {
  return (
    <div className="space-y-6">
      <div>
        <p className="text-sm font-medium text-charcoal/60">Good morning,</p>
        <h1 className="text-3xl font-black leading-tight text-ink">
          Let's eat plants today.
        </h1>
      </div>

      <div className="grid grid-cols-2 gap-3">
        <PlaceholderCard
          tone="leaf"
          title="Today's plan"
          subtitle="Tofu scramble · Buddha bowl · Lentil curry"
        />
        <PlaceholderCard
          tone="cyan"
          title="Reminder"
          subtitle="Drink 2L of water"
        />
      </div>

      <section>
        <SectionHeader title="Quick actions" />
        <div className="grid grid-cols-3 gap-3">
          <PlaceholderCard title="Log meal" tone="sage" />
          <PlaceholderCard title="Scan label" tone="cyan" />
          <PlaceholderCard title="Find spot" tone="violet" />
        </div>
      </section>

      <section>
        <SectionHeader title="For you" href="/recipes" />
        <div className="space-y-3">
          <PlaceholderCard
            title="High-protein chickpea bowl"
            subtitle="25 min · 480 kcal · 28g protein"
            tone="leaf"
          />
          <PlaceholderCard
            title="Creamy cashew pasta"
            subtitle="20 min · 520 kcal · 18g protein"
            tone="cyan"
          />
        </div>
      </section>
    </div>
  );
}
