/*
 * LandingSlider.tsx
 * ----------------------------------------------------------------------------
 * Fullscreen, paginated landing for Sproutly. Modeled on a direction-aware
 * Framer Motion slider:
 *
 *  - Big word is split in two halves that fly past each other in opposite
 *    directions. We pass `dir` (1 = forward, -1 = back) through AnimatePresence
 *    `custom` so each variant can flip its sign based on the swipe direction.
 *  - The hero image sits BEHIND the word (z-10 vs z-20) so the title stays
 *    legible without a drop-shadow blob.
 *  - The info card lands AFTER the image via `delay`, teaching staggering.
 *  - `<AnimatePresence mode="popLayout">` lets exiting elements animate out
 *    without shoving siblings around.
 *  - Each animated element has a `key` tied to the active index — that's
 *    what triggers AnimatePresence's exit + enter cycle.
 *
 * Mobile-first: type uses `clamp(...)` and `vmin` so the layout breathes from
 * a 360px phone up to a desktop browser.
 */

import { useCallback, useEffect, useRef, useState } from "react";
import { AnimatePresence, motion, type Variants } from "framer-motion";
import { useNavigate } from "react-router-dom";
import { ChevronUp, ChevronDown, Menu } from "lucide-react";
import { PILLARS } from "../data/pillars";

// Shared easing — a soft "swoosh" curve used everywhere for a coherent feel.
const EASE: [number, number, number, number] = [0.7, 0, 0.2, 1];
// Lock duration: ignore new input while a transition is in flight.
const LOCK_MS = 900;

// === VARIANTS ===
// Each variant function reads `(custom)` (the direction) so the SAME variant
// flips its sign for forward vs backward navigation.

// LEFT half of the word: forward = enter from BELOW, exit UP. Backward inverts.
const leftWordVariants: Variants = {
  enter: (dir: number) => ({ y: dir * 220, opacity: 0 }),
  center: { y: 0, opacity: 1 },
  exit: (dir: number) => ({ y: dir * -220, opacity: 0 }),
};

// RIGHT half does the OPPOSITE of the left — that's what creates the
// "two halves passing each other" feel.
const rightWordVariants: Variants = {
  enter: (dir: number) => ({ y: dir * -220, opacity: 0 }),
  center: { y: 0, opacity: 1 },
  exit: (dir: number) => ({ y: dir * 220, opacity: 0 }),
};

// Hero image: arrives with scale + rotate for life, leaves with opposite sign.
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

// Info card: simple fade + slide; delay makes it land AFTER the hero.
const cardVariants: Variants = {
  enter: { y: 40, opacity: 0 },
  center: { y: 0, opacity: 1 },
  exit: { y: -20, opacity: 0 },
};

export default function LandingSlider() {
  // === STATE ===
  // Active pillar index — drives every animation via the `key` prop pattern.
  const [index, setIndex] = useState(0);
  // Direction of the most recent transition. Passed to AnimatePresence `custom`.
  const [dir, setDir] = useState<1 | -1>(1);

  // 900ms lock: a useRef flag (NOT state) because we don't need to re-render
  // when the lock flips, we just need a "is one animation in flight?" check.
  const locked = useRef(false);
  // Track touch start Y for swipe detection.
  const touchStartY = useRef<number | null>(null);

  const navigate = useNavigate();
  const pillar = PILLARS[index];

  // Paginate by +1 or -1, wrapping at the ends. Honors the lock so spamming
  // the wheel doesn't queue up a stack of transitions.
  const paginate = useCallback((delta: 1 | -1) => {
    if (locked.current) return;
    locked.current = true;
    setDir(delta);
    setIndex((i) => (i + delta + PILLARS.length) % PILLARS.length);
    setTimeout(() => {
      locked.current = false;
    }, LOCK_MS);
  }, []);

  // === SIDE EFFECTS ===

  // Animate body background to current pillar color. We mutate <body> directly
  // because it lives outside the React tree; the CSS transition in index.css
  // tweens the change for us.
  useEffect(() => {
    document.body.style.backgroundColor = pillar.bg;
    document.body.style.color = pillar.fg;
  }, [pillar]);

  // === INPUT HANDLERS ===
  // Wheel + keyboard listeners live on window so they fire anywhere on the page.
  useEffect(() => {
    const onWheel = (e: WheelEvent) => {
      if (Math.abs(e.deltaY) < 8) return; // ignore trackpad jitter
      paginate(e.deltaY > 0 ? 1 : -1);
    };
    const onKey = (e: KeyboardEvent) => {
      if (e.key === "ArrowDown" || e.key === "PageDown") paginate(1);
      if (e.key === "ArrowUp" || e.key === "PageUp") paginate(-1);
    };
    window.addEventListener("wheel", onWheel, { passive: true });
    window.addEventListener("keydown", onKey);
    return () => {
      window.removeEventListener("wheel", onWheel);
      window.removeEventListener("keydown", onKey);
    };
  }, [paginate]);

  // Touch handlers attached to the root <section> below.
  const onTouchStart = (e: React.TouchEvent) => {
    touchStartY.current = e.touches[0]?.clientY ?? null;
  };
  const onTouchEnd = (e: React.TouchEvent) => {
    if (touchStartY.current == null) return;
    const dy = (e.changedTouches[0]?.clientY ?? 0) - touchStartY.current;
    if (Math.abs(dy) > 40) paginate(dy < 0 ? 1 : -1); // swipe up = forward
    touchStartY.current = null;
  };

  // Split the title into two halves at the pillar's `splitAt` index.
  const leftHalf = pillar.name.slice(0, pillar.splitAt);
  const rightHalf = pillar.name.slice(pillar.splitAt);

  // === RENDER ===
  return (
    <section
      className="relative h-[100dvh] w-screen overflow-hidden select-none"
      onTouchStart={onTouchStart}
      onTouchEnd={onTouchEnd}
      style={{ color: pillar.fg }}
    >
      {/* Top bar: wordmark, nav (hidden on phones), menu. z-30 keeps it above hero. */}
      <header className="absolute inset-x-0 top-0 z-30 flex items-center justify-between px-5 pt-[max(1rem,env(safe-area-inset-top))] sm:px-10">
        <div className="flex items-center gap-2 font-display text-2xl font-extrabold tracking-tight">
          <span className="inline-block h-2.5 w-2.5 rounded-full bg-current opacity-90" />
          Sproutly
        </div>
        <nav className="hidden gap-7 text-xs font-semibold uppercase tracking-[0.18em] sm:flex">
          <a href="#discover">Discover</a>
          <a href="#products">Products</a>
          <a href="#map">Map</a>
          <a href="#recipes">Recipes</a>
        </nav>
        <button
          aria-label="Menu"
          className="grid h-10 w-10 place-items-center rounded-full border border-current/40 backdrop-blur-sm transition hover:bg-current/10"
        >
          <Menu className="h-4 w-4" />
        </button>
      </header>

      {/* AnimatePresence drives exit + enter when `key` changes.
          custom={dir} forwards the direction to every variant. */}
      <AnimatePresence custom={dir} mode="popLayout" initial={false}>
        {/* LEFT half of the title — z-20 so it sits above the hero (z-10). */}
        <motion.h1
          // key tied to index = "this is a different element each step",
          // which is what tells AnimatePresence to exit + re-enter.
          key={`left-${index}`}
          custom={dir}
          variants={leftWordVariants}
          // initial / animate / exit pull from variants by name.
          // Try changing exit to {opacity: 0} to kill the upward sweep.
          initial="enter"
          animate="center"
          exit="exit"
          // transition: shared swoosh easing, long-ish duration for drama.
          transition={{ duration: 0.9, ease: EASE }}
          className="pointer-events-none absolute top-1/2 z-20 -translate-y-1/2 font-display font-extrabold leading-none tracking-[-0.04em]"
          style={{
            left: "5vw",
            fontSize: "clamp(4.5rem, 22vw, 18rem)",
          }}
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
          style={{
            right: "5vw",
            fontSize: "clamp(4.5rem, 22vw, 18rem)",
          }}
        >
          {rightHalf}
        </motion.h1>

        {/* Hero image. z-10 (under the text). No drop-shadow — it makes a dark
            blob behind transparent PNGs and is unnecessary here. */}
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

        {/* Info card: delayed so it lands AFTER the hero settles. */}
        <motion.div
          key={`card-${index}`}
          variants={cardVariants}
          initial="enter"
          animate="center"
          exit="exit"
          // delay teaches staggering — the card is part of the same scene
          // but arrives a beat later for a deliberate reveal.
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

      {/* Right-side vertical controls. Hidden on the smallest phones. */}
      <div className="absolute bottom-28 right-4 z-30 hidden flex-col gap-3 sm:flex">
        <button
          aria-label="Previous"
          onClick={() => paginate(-1)}
          className="grid h-11 w-11 place-items-center rounded-full border border-current/40 transition hover:bg-current/10"
        >
          <ChevronUp className="h-4 w-4" />
        </button>
        <button
          aria-label="Next"
          onClick={() => paginate(1)}
          className="grid h-11 w-11 place-items-center rounded-full border border-current/40 transition hover:bg-current/10"
        >
          <ChevronDown className="h-4 w-4" />
        </button>
      </div>

      {/* Dot pager — vertical on desktop, horizontal on phones. */}
      <div className="absolute right-5 top-1/2 z-30 hidden -translate-y-1/2 flex-col gap-2 sm:flex">
        {PILLARS.map((p, i) => (
          <button
            key={p.id}
            aria-label={`Go to ${p.name}`}
            onClick={() => {
              if (i === index || locked.current) return;
              setDir(i > index ? 1 : -1);
              locked.current = true;
              setIndex(i);
              setTimeout(() => (locked.current = false), LOCK_MS);
            }}
            className="h-2 w-2 rounded-full transition"
            style={{
              backgroundColor: "currentColor",
              opacity: i === index ? 1 : 0.35,
              transform: i === index ? "scale(1.4)" : "scale(1)",
            }}
          />
        ))}
      </div>

      {/* Mobile bottom controls: prev / counter / next in a single row. */}
      <div
        className="absolute inset-x-0 z-30 flex items-center justify-between px-6 sm:justify-center sm:gap-8"
        style={{
          bottom: "max(1.25rem, env(safe-area-inset-bottom))",
        }}
      >
        <button
          aria-label="Previous"
          onClick={() => paginate(-1)}
          className="grid h-11 w-11 place-items-center rounded-full border border-current/40 sm:hidden"
        >
          <ChevronUp className="h-4 w-4" />
        </button>
        <div className="font-display text-sm tracking-[0.3em] opacity-80">
          {String(index + 1).padStart(2, "0")} /{" "}
          {String(PILLARS.length).padStart(2, "0")}
        </div>
        <button
          aria-label="Next"
          onClick={() => paginate(1)}
          className="grid h-11 w-11 place-items-center rounded-full border border-current/40 sm:hidden"
        >
          <ChevronDown className="h-4 w-4" />
        </button>
      </div>
    </section>
  );
}
