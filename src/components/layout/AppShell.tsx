// AppShell: the phone-shaped frame used by every in-app route.
// On a desktop browser it renders a 420px-wide "device" centered on a
// neutral background so the app always feels mobile. On a real phone it
// fills the viewport edge-to-edge.
import { Outlet } from "react-router-dom";
import TopBar from "./TopBar";
import BottomNav from "./BottomNav";

export default function AppShell() {
  return (
    <div className="min-h-[100dvh] w-full bg-[oklch(0.94_0.02_140)]">
      <div className="mx-auto flex min-h-[100dvh] w-full max-w-[440px] flex-col bg-cream shadow-[0_30px_80px_-20px_oklch(0.32_0.06_150/0.25)] sm:my-6 sm:min-h-[calc(100dvh-3rem)] sm:rounded-[2.25rem] sm:ring-1 sm:ring-black/5">
        <TopBar />
        <main className="flex-1 overflow-y-auto px-5 pb-28 pt-4">
          <Outlet />
        </main>
        <BottomNav />
      </div>
    </div>
  );
}
