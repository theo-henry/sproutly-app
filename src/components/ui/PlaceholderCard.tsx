// Minimal placeholder used by skeleton pages until real data ships.
import { Interactive } from "./Motion";

type Props = {
  title: string;
  subtitle?: string;
  tone?: "leaf" | "moss" | "mint" | "sage";
  meta?: string;
};

export default function PlaceholderCard({ title, subtitle, tone = "sage", meta }: Props) {
  const styles = {
    leaf: "border-leaf/35 bg-leaf/12",
    moss: "border-lichen/35 bg-lichen/14",
    mint: "border-mint/25 bg-mint/10",
    sage: "border-line/70 bg-panel-soft/58",
  }[tone];

  return (
    <Interactive>
      <div className={`relative overflow-hidden rounded-2xl border ${styles} p-4 shadow-[0_18px_70px_-50px_black]`}>
        <div className="pointer-events-none absolute inset-x-0 top-0 h-px bg-gradient-to-r from-transparent via-mint/35 to-transparent" />
        {meta ? (
          <div className="mb-4 text-[0.62rem] font-black uppercase tracking-[0.22em] text-leaf/75">
            {meta}
          </div>
        ) : null}
        <div className="text-base font-black text-ink">{title}</div>
        {subtitle ? (
          <div className="mt-2 text-sm leading-5 text-charcoal">{subtitle}</div>
        ) : null}
      </div>
    </Interactive>
  );
}
