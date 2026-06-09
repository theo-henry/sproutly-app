import { Link } from "react-router-dom";
import { Search, Sprout, UserRound } from "lucide-react";
import { motion } from "framer-motion";
import { useEffect, useMemo, useState } from "react";
import { useAuth } from "../../auth/AuthContext";
import { getAvatarUrl, loadProfile, type Profile } from "../../lib/profiles";

export default function TopBar() {
  const { user } = useAuth();
  const [profile, setProfile] = useState<Profile | null>(null);

  useEffect(() => {
    if (!user) return undefined;

    let mounted = true;

    async function refreshProfile() {
      try {
        const nextProfile = await loadProfile(user!.id);
        if (!mounted) return;
        setProfile(nextProfile);
      } catch {
        if (!mounted) return;
        setProfile(null);
      }
    }

    void refreshProfile();
    window.addEventListener("sproutly-profile-updated", refreshProfile);

    return () => {
      mounted = false;
      window.removeEventListener("sproutly-profile-updated", refreshProfile);
    };
  }, [user]);

  const avatarUrl = useMemo(
    () => getAvatarUrl(profile?.avatar_path ?? null),
    [profile?.avatar_path],
  );
  const initials =
    (profile?.display_name || user?.email || "A")
      .slice(0, 1)
      .toUpperCase() || "A";

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
        <motion.div
          whileHover={{ y: -2 }}
          whileTap={{ scale: 0.96 }}
        >
          <Link
            to="/account"
            aria-label="Open account settings"
            className="grid h-10 w-10 place-items-center overflow-hidden rounded-full bg-panel-soft/70 text-mint ring-1 ring-line/70 transition hover:bg-leaf/15"
          >
            {avatarUrl ? (
              <img src={avatarUrl} alt="" className="h-full w-full object-cover" />
            ) : profile?.display_name || user?.email ? (
              <span className="text-sm font-black text-leaf">{initials}</span>
            ) : (
              <UserRound className="h-4 w-4" />
            )}
          </Link>
        </motion.div>
      </div>
    </header>
  );
}
