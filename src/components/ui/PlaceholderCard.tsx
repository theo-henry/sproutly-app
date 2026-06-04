// Minimal placeholder used by skeleton pages until real data ships.
type Props = { title: string; subtitle?: string; tone?: "leaf" | "cyan" | "violet" | "sage" };

export default function PlaceholderCard({ title, subtitle, tone = "sage" }: Props) {
  const bg = {
    leaf: "bg-leaf/10 border-leaf/20",
    cyan: "bg-cyan/10 border-cyan/20",
    violet: "bg-violet/10 border-violet/20",
    sage: "bg-white border-line",
  }[tone];
  return (
    <div className={`rounded-lg border ${bg} p-4 shadow-[0_16px_50px_-45px_black]`}>
      <div className="text-base font-black text-ink">{title}</div>
      {subtitle ? (
        <div className="mt-1 text-sm leading-5 text-charcoal/65">{subtitle}</div>
      ) : null}
    </div>
  );
}
