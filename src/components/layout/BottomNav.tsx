import { NavLink } from "react-router-dom";
import { Home, ShoppingBag, MapPin, ChefHat } from "lucide-react";

// 4-tab bottom navigation matching Sproutly's pillars.
// Tabs use NavLink so the active route gets an `isActive` style.
const TABS = [
  { to: "/", label: "Home", Icon: Home },
  { to: "/products", label: "Products", Icon: ShoppingBag },
  { to: "/map", label: "Nearby", Icon: MapPin },
  { to: "/recipes", label: "Recipes", Icon: ChefHat },
];

export default function BottomNav() {
  return (
    <nav
      className="sticky bottom-0 z-20 mt-auto border-t border-line bg-white/88 backdrop-blur-xl"
      style={{ paddingBottom: "max(0.5rem, env(safe-area-inset-bottom))" }}
    >
      <ul className="flex items-stretch justify-around px-2 pt-2">
        {TABS.map(({ to, label, Icon }) => (
          <li key={to} className="flex-1">
            <NavLink
              to={to}
              className={({ isActive }) =>
                `flex flex-col items-center gap-1 rounded-lg px-2 py-1.5 text-[0.7rem] font-bold transition ${
                  isActive ? "bg-ink text-white" : "text-charcoal/55 hover:bg-mist"
                }`
              }
            >
              {({ isActive }) => (
                <>
                  <Icon
                    className="h-5 w-5"
                    strokeWidth={isActive ? 2.4 : 1.8}
                  />
                  <span>{label}</span>
                </>
              )}
            </NavLink>
          </li>
        ))}
      </ul>
    </nav>
  );
}
