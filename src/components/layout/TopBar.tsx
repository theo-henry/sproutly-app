import { Link } from "react-router-dom";
import { Bell, Search } from "lucide-react";

// Sticky-feeling top bar inside the phone frame. Greeting + quick actions.
export default function TopBar() {
  return (
    <header className="flex items-center justify-between px-5 pt-[max(1rem,env(safe-area-inset-top))]">
      <Link to="/home" className="flex items-center gap-2">
        <span className="grid h-9 w-9 place-items-center rounded-full bg-leaf text-cream">
          <span className="font-display text-lg font-extrabold">S</span>
        </span>
        <span className="font-display text-lg font-extrabold tracking-tight text-forest">
          Sproutly
        </span>
      </Link>
      <div className="flex items-center gap-2">
        <button
          aria-label="Search"
          className="grid h-10 w-10 place-items-center rounded-full bg-white text-forest ring-1 ring-black/5"
        >
          <Search className="h-4 w-4" />
        </button>
        <button
          aria-label="Notifications"
          className="grid h-10 w-10 place-items-center rounded-full bg-white text-forest ring-1 ring-black/5"
        >
          <Bell className="h-4 w-4" />
        </button>
      </div>
    </header>
  );
}
