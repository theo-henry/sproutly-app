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
      <h2 className="font-display text-xl font-extrabold text-forest">{title}</h2>
      {href ? (
        <Link
          to={href}
          className="text-xs font-semibold uppercase tracking-wider text-leaf"
        >
          {action}
        </Link>
      ) : null}
    </div>
  );
}
