import { NavLink } from "react-router-dom";
import { Home, ShoppingBag, MapPin, ChefHat } from "lucide-react";
import { motion } from "framer-motion";

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
      className="sticky bottom-0 z-20 mt-auto border-t border-line/60 bg-void/72 backdrop-blur-xl"
      style={{ paddingBottom: "max(0.5rem, env(safe-area-inset-bottom))" }}
    >
      <ul className="mx-auto flex max-w-3xl items-stretch justify-around px-2 pt-2">
        {TABS.map(({ to, label, Icon }) => (
          <li key={to} className="flex-1">
            <NavLink
              to={to}
              className={({ isActive }) =>
                `relative flex flex-col items-center gap-1 rounded-xl px-2 py-2 text-[0.7rem] font-black transition ${
                  isActive ? "text-ink" : "text-charcoal/70 hover:text-mint"
                }`
              }
            >
              {({ isActive }) => (
                <>
                  {isActive ? (
                    <motion.span
                      layoutId="active-tab"
                      className="absolute inset-1 rounded-xl bg-leaf"
                      transition={{ duration: 0.35, ease: [0.22, 1, 0.36, 1] }}
                    />
                  ) : null}
                  <Icon
                    className="relative h-5 w-5"
                    strokeWidth={isActive ? 2.4 : 1.8}
                  />
                  <span className="relative">{label}</span>
                </>
              )}
            </NavLink>
          </li>
        ))}
      </ul>
    </nav>
  );
}
