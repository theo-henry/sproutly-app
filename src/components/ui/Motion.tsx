import { motion, type Variants } from "framer-motion";
import type { ReactNode } from "react";

type RevealMode = "rise" | "drift" | "scale" | "wipe" | "soft";

const easing: [number, number, number, number] = [0.22, 1, 0.36, 1];

const revealVariants: Record<RevealMode, Variants> = {
  rise: {
    hidden: { opacity: 0, y: 28, filter: "blur(10px)" },
    show: { opacity: 1, y: 0, filter: "blur(0px)" },
  },
  drift: {
    hidden: { opacity: 0, x: -24, filter: "blur(8px)" },
    show: { opacity: 1, x: 0, filter: "blur(0px)" },
  },
  scale: {
    hidden: { opacity: 0, scale: 0.94, y: 12 },
    show: { opacity: 1, scale: 1, y: 0 },
  },
  wipe: {
    hidden: { opacity: 0, clipPath: "inset(0 100% 0 0)" },
    show: { opacity: 1, clipPath: "inset(0 0% 0 0)" },
  },
  soft: {
    hidden: { opacity: 0 },
    show: { opacity: 1 },
  },
};

export function Reveal({
  children,
  mode = "rise",
  delay = 0,
  className,
}: {
  children: ReactNode;
  mode?: RevealMode;
  delay?: number;
  className?: string;
}) {
  return (
    <motion.div
      variants={revealVariants[mode]}
      initial="hidden"
      whileInView="show"
      viewport={{ once: true, margin: "-80px" }}
      transition={{ duration: 0.72, delay, ease: easing }}
      className={className}
    >
      {children}
    </motion.div>
  );
}

export function Stagger({
  children,
  className,
}: {
  children: ReactNode;
  className?: string;
}) {
  return (
    <motion.div
      initial="hidden"
      whileInView="show"
      viewport={{ once: true, margin: "-80px" }}
      variants={{
        hidden: {},
        show: { transition: { staggerChildren: 0.08 } },
      }}
      className={className}
    >
      {children}
    </motion.div>
  );
}

export function StaggerItem({
  children,
  className,
  mode = "rise",
}: {
  children: ReactNode;
  className?: string;
  mode?: RevealMode;
}) {
  return (
    <motion.div
      variants={revealVariants[mode]}
      transition={{ duration: 0.68, ease: easing }}
      className={className}
    >
      {children}
    </motion.div>
  );
}

export function Interactive({
  children,
  className,
}: {
  children: ReactNode;
  className?: string;
}) {
  return (
    <motion.div
      whileHover={{ y: -4, scale: 1.01 }}
      whileTap={{ scale: 0.985 }}
      transition={{ duration: 0.22, ease: "easeOut" }}
      className={className}
    >
      {children}
    </motion.div>
  );
}

export function PageTitle({
  eyebrow,
  title,
  body,
  action,
}: {
  eyebrow: string;
  title: string;
  body?: string;
  action?: ReactNode;
}) {
  const words = title.split(" ");

  return (
    <header className="grid gap-5 border-b border-line/60 pb-6 sm:grid-cols-[1fr_auto] sm:items-end">
      <div>
        <Reveal mode="wipe">
          <p className="text-xs font-black uppercase tracking-[0.26em] text-leaf/80">
            {eyebrow}
          </p>
        </Reveal>
        <motion.h1
          initial="hidden"
          animate="show"
          variants={{ show: { transition: { staggerChildren: 0.045 } } }}
          className="mt-3 max-w-3xl text-4xl font-black leading-none text-ink sm:text-6xl"
        >
          {words.map((word, index) => (
            <motion.span
              key={`${title}-${word}-${index}`}
              variants={{
                hidden: { opacity: 0, y: 18, filter: "blur(8px)" },
                show: { opacity: 1, y: 0, filter: "blur(0px)" },
              }}
              transition={{ duration: 0.58, ease: easing }}
              className="mr-3 inline-block"
            >
              {word}
            </motion.span>
          ))}
        </motion.h1>
        {body ? (
          <Reveal delay={0.12} mode="drift">
            <p className="mt-4 max-w-2xl text-sm leading-6 text-charcoal sm:text-base">
              {body}
            </p>
          </Reveal>
        ) : null}
      </div>
      {action ? <Reveal mode="scale">{action}</Reveal> : null}
    </header>
  );
}
