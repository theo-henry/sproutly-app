// Minimal placeholder used by skeleton pages until real data ships.
type Props = { title: string; subtitle?: string; tone?: "leaf" | "clay" | "sage" };

export default function PlaceholderCard({ title, subtitle, tone = "sage" }: Props) {
  const bg = {
    leaf: "bg-leaf/15",
    clay: "bg-clay/15",
    sage: "bg-sage/25",
  }[tone];
  return (
    <div className={`rounded-2xl ${bg} p-4 ring-1 ring-black/5`}>
      <div className="font-display text-base font-bold text-forest">{title}</div>
      {subtitle ? (
        <div className="mt-1 text-sm text-forest/70">{subtitle}</div>
      ) : null}
    </div>
  );
}
