import { Link } from "react-router-dom";
import SectionHeader from "../components/ui/SectionHeader";
import PlaceholderCard from "../components/ui/PlaceholderCard";
import { PageTitle, Reveal, Stagger, StaggerItem } from "../components/ui/Motion";

// Recipes tab — browsable recipe library; entry point to meal planning.
export default function Recipes() {
  return (
    <div className="space-y-8">
      <PageTitle
        eyebrow="Recipes"
        title="Cook from a smarter plant-based library."
        body="Recipes that work for your diet, pantry, timing, and weekly plan."
        action={
        <Link
          to="/meal-plan"
            className="rounded-full bg-leaf px-5 py-3 text-xs font-black uppercase tracking-[0.18em] text-void transition hover:bg-mint"
        >
          AI meal plan
        </Link>
        }
      />

      <Reveal mode="wipe">
        <div className="rounded-[1.5rem] border border-line/70 bg-panel-soft/60 p-5">
          <p className="text-xs font-black uppercase tracking-[0.24em] text-leaf/75">
            Tonight
          </p>
          <h2 className="mt-3 text-3xl font-black text-ink">High-protein, low-prep, no compromise.</h2>
        </div>
      </Reveal>

      <section>
        <SectionHeader title="Quick & easy" />
        <Stagger className="grid gap-3">
          <StaggerItem mode="scale">
            <PlaceholderCard title="15-min peanut noodles" subtitle="One pan · 12g protein" tone="moss" meta="Fast" />
          </StaggerItem>
          <StaggerItem mode="rise">
            <PlaceholderCard title="Lemon herb tofu wrap" subtitle="10 min · high fiber" tone="leaf" meta="Fresh" />
          </StaggerItem>
          <StaggerItem mode="drift">
            <PlaceholderCard title="Chickpea shakshuka" subtitle="20 min · iron-rich" tone="mint" meta="Iron" />
          </StaggerItem>
        </Stagger>
      </section>

      <section>
        <SectionHeader title="Seasonal picks" />
        <Stagger className="grid grid-cols-1 gap-3">
          <StaggerItem mode="rise">
          <PlaceholderCard title="Roasted squash" tone="leaf" />
          </StaggerItem>
          <StaggerItem mode="scale">
            <PlaceholderCard title="Charred greens salad" tone="moss" />
          </StaggerItem>
        </Stagger>
      </section>
    </div>
  );
}
