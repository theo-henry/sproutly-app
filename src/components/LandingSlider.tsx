/*
 * LandingSlider.tsx — the animated hero on the landing page.
 * ----------------------------------------------------------------------------
 * Self-contained hero section that paginates between Sproutly's four pillars.
 * Key design points:
 *
 *  - Direction-aware split-word animation: the title splits in two halves
 *    and they fly past each other. A `dir` value (1 forward, -1 back) is
 *    passed through AnimatePresence's `custom` prop so variants flip sign.
 *  - This hero NO LONGER hijacks the page wheel / keyboard / swipe — the
 *    page below it has real content the user needs to scroll to, so we
 *    rely on auto-advance + prev/next buttons + dot pager instead.
 *  - The animated background color now lives on the hero ITSELF (a
 *    motion.div tweens its backgroundColor) instead of <body>, so the
 *    rest of the page keeps its cream theme.
 *  - Every animated element has a `key` tied to the active index — that's
 *    what triggers AnimatePresence's exit + enter cycle.
 */

import { useCallback, useEffect, useRef, useState } from "react";
import { AnimatePresence, motion, type Variants } from "framer-motion";
import { useNavigate } from "react-router-dom";
import { ChevronLeft, ChevronRight } from "lucide-react";
import { PILLARS } from "../data/pillars";

// Shared easing — a soft "swoosh" curve used everywhere for a coherent feel.
const EASE: [number, number, number, number] = [0.7, 0, 0.2, 1];
// Lock duration: ignore new input while a transition is in flight.
const LOCK_MS = 900;
// Auto-advance every N ms when the user isn't interacting.
const AUTO_MS = 5500;

// === VARIANTS ===

// LEFT half of the word: forward = enter from BELOW, exit UP.
const leftWordVariants: Variants = {
  enter: (dir: number) => ({ y: dir * 220, opacity: 0 }),
  center: { y: 0, opacity: 1 },
  exit: (dir: number) => ({ y: dir * -220, opacity: 0 }),
};

// RIGHT half does the OPPOSITE of the left.
const rightWordVariants: Variants = {
  enter: (dir: number) => ({ y: dir * -220, opacity: 0 }),
  center: { y: 0, opacity: 1 },
  exit: (dir: number) => ({ y: dir * 220, opacity: 0 }),
};

// Hero image: scale + rotate for life.
const heroVariants: Variants = {
  enter: (dir: number) => ({
    y: dir * 360,
    scale: 0.7,
    rotate: dir * 18,
    opacity: 0,
  }),
  center: { y: 0, scale: 1, rotate: 0, opacity: 1 },
  exit: (dir: number) => ({
    y: dir * -360,
    scale: 0.7,
    rotate: dir * -18,
    opacity: 0,
  }),
};

// Info card slides + fades, delayed so it lands AFTER the hero.
const cardVariants: Variants = {
  enter: { y: 40, opacity: 0 },
  center: { y: 0, opacity: 1 },
  exit: { y: -20, opacity: 0 },
};

export default function LandingSlider() {
  // === STATE ===
  const [index, setIndex] = useState(0);
  const [dir, setDir] = useState<1 | -1>(1);

  // Animation lock (ref, not state — no re-render needed).
  const locked = useRef(false);
  // Pause auto-advance briefly after user interacts.
  const pausedUntil = useRef(0);

  const navigate = useNavigate();
  const pillar = PILLARS[index];

  // Paginate by +1 or -1, wrapping at the ends.
  const paginate = useCallback((delta: 1 | -1) => {
    if (locked.current) return;
    locked.current = true;
    pausedUntil.current = Date.now() + 8000;
    setDir(delta);
    setIndex((i) => (i + delta + PILLARS.length) % PILLARS.length);
    setTimeout(() => {
      locked.current = false;
    }, LOCK_MS);
  }, []);

  // Jump to a specific index (used by the dot pager).
  const jumpTo = useCallback(
    (i: number) => {
      if (i === index || locked.current) return;
      locked.current = true;
      pausedUntil.current = Date.now() + 8000;
      setDir(i > index ? 1 : -1);
      setIndex(i);
      setTimeout(() => {
        locked.current = false;
      }, LOCK_MS);
    },
    [index],
  );

  // Auto-advance loop. Skips when user just interacted.
  useEffect(() => {
    const id = setInterval(() => {
      if (Date.now() < pausedUntil.current) return;
      if (document.hidden) return;
      paginate(1);
    }, AUTO_MS);
    return () => clearInterval(id);
  }, [paginate]);

  // Split the title into two halves at the pillar's `splitAt` index.
  const leftHalf = pillar.name.slice(0, pillar.splitAt);
  const rightHalf = pillar.name.slice(pillar.splitAt);

  // === RENDER ===
  return (
    <motion.section
      // animate backgroundColor lives on the hero itself, not <body>,
      // so the rest of the page keeps its cream background.
      animate={{ backgroundColor: pillar.bg, color: pillar.fg }}
      transition={{ duration: 0.9, ease: EASE }}
      className="relative h-[100dvh] w-full overflow-hidden select-none"
    >
      {/* Wordmark — purely decorative on the hero. */}
      <header className="absolute inset-x-0 top-0 z-30 flex items-center justify-between px-5 pt-[max(1rem,env(safe-area-inset-top))] sm:px-10">
        <div className="flex items-center gap-2 font-display text-2xl font-extrabold tracking-tight">
          <span className="inline-block h-2.5 w-2.5 rounded-full bg-current opacity-90" />
          Sproutly
        </div>
        <nav className="hidden gap-7 text-xs font-semibold uppercase tracking-[0.18em] sm:flex">
          <a href="#about">About</a>
          <a href="#pillars">Features</a>
          <a href="#how">How it works</a>
          <a href="#cta">Get the app</a>
        </nav>
        <button
          onClick={() => navigate("/home")}
          className="rounded-full bg-current px-4 py-2 text-xs font-semibold"
          style={{ color: pillar.bg }}
        >
          Open app
        </button>
      </header>

      {/* AnimatePresence drives exit + enter when `key` changes.
          custom={dir} forwards the direction to every variant. */}
      <AnimatePresence custom={dir} mode="popLayout" initial={false}>
        {/* LEFT half of the title — z-20 above the hero image (z-10). */}
        <motion.h1
          key={`left-${index}`}
          custom={dir}
          variants={leftWordVariants}
          initial="enter"
          animate="center"
          exit="exit"
          transition={{ duration: 0.9, ease: EASE }}
          className="pointer-events-none absolute top-1/2 z-20 -translate-y-1/2 font-display font-extrabold leading-none tracking-[-0.04em]"
          style={{ left: "5vw", fontSize: "clamp(4.5rem, 22vw, 18rem)" }}
        >
          {leftHalf}
        </motion.h1>

        {/* RIGHT half — mirrored variants. */}
        <motion.h1
          key={`right-${index}`}
          custom={dir}
          variants={rightWordVariants}
          initial="enter"
          animate="center"
          exit="exit"
          transition={{ duration: 0.9, ease: EASE }}
          className="pointer-events-none absolute top-1/2 z-20 -translate-y-1/2 font-display font-extrabold leading-none tracking-[-0.04em]"
          style={{ right: "5vw", fontSize: "clamp(4.5rem, 22vw, 18rem)" }}
        >
          {rightHalf}
        </motion.h1>

        {/* Hero image. z-10 (under the text). */}
        <motion.img
          key={`hero-${index}`}
          src={pillar.image}
          alt={pillar.name}
          custom={dir}
          variants={heroVariants}
          initial="enter"
          animate="center"
          exit="exit"
          transition={{ duration: 0.9, ease: EASE }}
          className="absolute left-1/2 top-1/2 z-10 -translate-x-1/2 -translate-y-1/2 rounded-[2.5rem] object-cover"
          style={{
            width: "min(72vmin, 520px)",
            height: "min(72vmin, 520px)",
          }}
          draggable={false}
        />

        {/* Info card: delayed so it lands AFTER the hero. */}
        <motion.div
          key={`card-${index}`}
          variants={cardVariants}
          initial="enter"
          animate="center"
          exit="exit"
          transition={{ duration: 0.5, delay: 0.28, ease: EASE }}
          className="absolute bottom-[max(7rem,calc(env(safe-area-inset-bottom)+5rem))] left-5 right-5 z-30 max-w-md sm:left-10 sm:right-auto"
        >
          <div className="rounded-3xl bg-black/15 p-5 backdrop-blur-md sm:bg-black/10">
            <div className="text-[0.7rem] font-semibold uppercase tracking-[0.22em] opacity-80">
              {pillar.kicker}
            </div>
            <p className="mt-2 text-base leading-snug sm:text-lg">
              {pillar.blurb}
            </p>
            <button
              onClick={() => navigate(pillar.route)}
              className="mt-4 inline-flex items-center gap-2 rounded-full bg-current px-5 py-2.5 text-sm font-semibold"
              style={{ color: pillar.bg }}
            >
              {pillar.cta}
              <span aria-hidden>→</span>
            </button>
          </div>
        </motion.div>
      </AnimatePresence>

      {/* Bottom controls: prev — dots — next. */}
      <div
        className="absolute inset-x-0 z-30 flex items-center justify-between px-6 sm:justify-center sm:gap-6"
        style={{ bottom: "max(1.5rem, env(safe-area-inset-bottom))" }}
      >
        <button
          aria-label="Previous"
          onClick={() => paginate(-1)}
          className="grid h-11 w-11 place-items-center rounded-full border border-current/40 transition hover:bg-current/10"
        >
          <ChevronLeft className="h-4 w-4" />
        </button>

        <div className="flex items-center gap-2">
          {PILLARS.map((p, i) => (
            <button
              key={p.id}
              aria-label={`Go to ${p.name}`}
              onClick={() => jumpTo(i)}
              className="h-2 rounded-full transition-all"
              style={{
                backgroundColor: "currentColor",
                width: i === index ? "1.75rem" : "0.5rem",
                opacity: i === index ? 1 : 0.4,
              }}
            />
          ))}
        </div>

        <button
          aria-label="Next"
          onClick={() => paginate(1)}
          className="grid h-11 w-11 place-items-center rounded-full border border-current/40 transition hover:bg-current/10"
        >
          <ChevronRight className="h-4 w-4" />
        </button>
      </div>

      {/* Scroll hint at the very bottom — invites users to keep reading. */}
      <motion.a
        href="#about"
        aria-label="Scroll for more"
        animate={{ y: [0, 6, 0] }}
        transition={{ duration: 1.8, repeat: Infinity, ease: "easeInOut" }}
        className="absolute bottom-2 left-1/2 z-30 hidden -translate-x-1/2 text-[0.65rem] font-semibold uppercase tracking-[0.3em] opacity-70 sm:block"
      >
        scroll ↓
      </motion.a>
    </motion.section>
  );
}
