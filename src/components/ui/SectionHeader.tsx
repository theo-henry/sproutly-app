// Reusable section header with a title + optional "see all" action.
import { Link } from "react-router-dom";

type Props = {
  title: string;
  href?: string;
  action?: string;
};

export default function SectionHeader({ title, href, action = "See all" }: Props) {
  return (
    <div className="mb-3 flex items-end justify-between">
      <h2 className="text-xl font-black text-ink">{title}</h2>
      {href ? (
        <Link
          to={href}
          className="text-xs font-bold uppercase tracking-[0.16em] text-leaf"
        >
          {action}
        </Link>
      ) : null}
    </div>
  );
}
