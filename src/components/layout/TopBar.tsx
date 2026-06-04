import { Link } from "react-router-dom";
import { Search, Sprout, UserRound } from "lucide-react";
import { motion } from "framer-motion";

export default function TopBar() {
  return (
    <header className="flex items-center justify-between border-b border-line/60 px-5 py-4">
      <Link to="/" className="group flex items-center gap-3">
        <span className="grid h-10 w-10 place-items-center rounded-xl bg-leaf/12 text-leaf ring-1 ring-leaf/25 transition group-hover:bg-leaf/18">
          <Sprout className="h-5 w-5" />
        </span>
        <span className="text-lg font-black text-ink">Sproutly</span>
      </Link>
      <div className="flex items-center gap-2">
        <motion.button
          aria-label="Search"
          whileHover={{ y: -2 }}
          whileTap={{ scale: 0.96 }}
          className="grid h-10 w-10 place-items-center rounded-full bg-panel-soft/70 text-mint ring-1 ring-line/70 transition hover:bg-leaf/15"
        >
          <Search className="h-4 w-4" />
        </motion.button>
        <motion.button
          aria-label="Profile"
          whileHover={{ y: -2 }}
          whileTap={{ scale: 0.96 }}
          className="grid h-10 w-10 place-items-center rounded-full bg-panel-soft/70 text-mint ring-1 ring-line/70 transition hover:bg-leaf/15"
        >
          <UserRound className="h-4 w-4" />
        </motion.button>
      </div>
    </header>
  );
}
