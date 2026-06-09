import { FormEvent, useMemo, useState } from "react";
import { Navigate, useLocation } from "react-router-dom";
import { Leaf, LogIn, Sprout, UserPlus } from "lucide-react";
import { motion } from "framer-motion";
import { useAuth } from "../auth/AuthContext";

type AuthMode = "login" | "signup";

export default function Login() {
  const location = useLocation();
  const { session, loading, hasConfig, hasDemo, signIn, signInDemo, signUp } =
    useAuth();
  const [mode, setMode] = useState<AuthMode>("login");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [busy, setBusy] = useState(false);
  const [message, setMessage] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  const from = useMemo(() => {
    const state = location.state as { from?: { pathname?: string } } | null;
    return state?.from?.pathname ?? "/";
  }, [location.state]);

  if (!loading && session) {
    return <Navigate to={from} replace />;
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setBusy(true);
    setError(null);
    setMessage(null);

    try {
      if (mode === "signup") {
        await signUp(email.trim(), password);
        setMessage("Account created. If email confirmation is enabled, verify your email before logging in.");
      } else {
        await signIn(email.trim(), password);
      }
    } catch (authError) {
      setError(authError instanceof Error ? authError.message : "Authentication failed.");
    } finally {
      setBusy(false);
    }
  }

  async function handleDemoLogin() {
    setBusy(true);
    setError(null);
    setMessage(null);

    try {
      await signInDemo();
    } catch (authError) {
      setError(authError instanceof Error ? authError.message : "Demo login failed.");
    } finally {
      setBusy(false);
    }
  }

  return (
    <main className="aurora-frame flex min-h-[100dvh] items-center justify-center px-5 py-8 text-ink">
      <section className="glass-frame w-full max-w-md overflow-hidden rounded-[2rem] border border-line/70 shadow-[0_30px_120px_oklch(0_0_0/0.35)]">
        <div className="relative overflow-hidden border-b border-line/60 p-6">
          <div className="absolute right-5 top-5 h-20 w-20 rounded-full border border-leaf/20" />
          <div className="absolute -right-2 top-14 h-10 w-10 rounded-full bg-leaf/18 blur-lg" />
          <div className="relative">
            <span className="grid h-12 w-12 place-items-center rounded-2xl bg-leaf/12 text-leaf ring-1 ring-leaf/25">
              <Sprout className="h-6 w-6" />
            </span>
            <p className="mt-6 text-xs font-black uppercase tracking-[0.28em] text-leaf/80">
              Sproutly
            </p>
            <h1 className="mt-3 text-4xl font-black leading-none text-ink">
              {mode === "login" ? "Welcome back." : "Create account."}
            </h1>
            <p className="mt-4 text-sm leading-6 text-charcoal">
              Save meal plans and keep your plant-based dashboard tied to your account.
            </p>
          </div>
        </div>

        <div className="grid gap-5 p-6">
          {!hasConfig ? (
            <div className="rounded-2xl border border-leaf/25 bg-leaf/10 p-4 text-sm leading-6 text-charcoal">
              Supabase is not configured yet. Add your project URL and publishable key to
              <span className="font-black text-ink"> .env.local</span>.
            </div>
          ) : null}

          <div className="grid grid-cols-2 gap-2 rounded-2xl border border-line/60 bg-void/42 p-1">
            {(["login", "signup"] as const).map((nextMode) => (
              <button
                key={nextMode}
                type="button"
                onClick={() => {
                  setMode(nextMode);
                  setError(null);
                  setMessage(null);
                }}
                className={`rounded-xl px-4 py-3 text-xs font-black uppercase tracking-[0.16em] transition ${
                  mode === nextMode
                    ? "bg-leaf text-void"
                    : "text-charcoal hover:bg-leaf/12 hover:text-mint"
                }`}
              >
                {nextMode === "login" ? "Log in" : "Sign up"}
              </button>
            ))}
          </div>

          <form className="grid gap-4" onSubmit={handleSubmit}>
            <label className="grid gap-2 text-sm font-bold text-charcoal">
              Email
              <input
                value={email}
                onChange={(event) => setEmail(event.target.value)}
                type="email"
                autoComplete="email"
                required
                className="rounded-2xl border border-line/70 bg-void/56 px-4 py-3 text-base font-bold text-ink outline-none transition placeholder:text-charcoal/45 focus:border-leaf/60 focus:ring-2 focus:ring-leaf/20"
                placeholder="you@example.com"
              />
            </label>

            <label className="grid gap-2 text-sm font-bold text-charcoal">
              Password
              <input
                value={password}
                onChange={(event) => setPassword(event.target.value)}
                type="password"
                autoComplete={mode === "login" ? "current-password" : "new-password"}
                required
                minLength={6}
                className="rounded-2xl border border-line/70 bg-void/56 px-4 py-3 text-base font-bold text-ink outline-none transition placeholder:text-charcoal/45 focus:border-leaf/60 focus:ring-2 focus:ring-leaf/20"
                placeholder="At least 6 characters"
              />
            </label>

            {error ? (
              <div className="rounded-2xl border border-red-300/30 bg-red-400/10 p-3 text-sm font-bold leading-5 text-red-100">
                {error}
              </div>
            ) : null}

            {message ? (
              <div className="rounded-2xl border border-leaf/30 bg-leaf/10 p-3 text-sm font-bold leading-5 text-mint">
                {message}
              </div>
            ) : null}

            <motion.button
              type="submit"
              disabled={busy || !hasConfig}
              whileHover={{ y: busy || !hasConfig ? 0 : -2 }}
              whileTap={{ scale: busy || !hasConfig ? 1 : 0.98 }}
              className="flex items-center justify-center gap-2 rounded-2xl bg-leaf px-5 py-4 text-sm font-black uppercase tracking-[0.18em] text-void transition hover:bg-mint disabled:cursor-not-allowed disabled:opacity-50"
            >
              {mode === "login" ? <LogIn className="h-4 w-4" /> : <UserPlus className="h-4 w-4" />}
              {busy ? "Working" : mode === "login" ? "Log in" : "Create account"}
            </motion.button>
          </form>

          <div className="grid gap-3 border-t border-line/60 pt-5">
            <button
              type="button"
              onClick={handleDemoLogin}
              disabled={busy || !hasConfig || !hasDemo}
              className="flex items-center justify-center gap-2 rounded-2xl border border-leaf/35 bg-leaf/12 px-5 py-4 text-sm font-black uppercase tracking-[0.16em] text-mint transition hover:bg-leaf/18 disabled:cursor-not-allowed disabled:opacity-50"
            >
              <Leaf className="h-4 w-4" />
              Log in with demo account
            </button>
            {!hasDemo ? (
              <p className="text-center text-xs leading-5 text-charcoal/80">
                Add demo credentials to enable the shared demo login button.
              </p>
            ) : null}
          </div>
        </div>
      </section>
    </main>
  );
}
