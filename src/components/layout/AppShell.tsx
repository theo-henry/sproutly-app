// AppShell: phone-preview frame on desktop, fullscreen on actual phones.
import { Outlet } from "react-router-dom";
import TopBar from "./TopBar";
import BottomNav from "./BottomNav";

export default function AppShell() {
  return (
    <div className="aurora-frame flex min-h-[100dvh] w-full items-center justify-center text-ink sm:p-5">
      <div className="glass-frame flex min-h-[100dvh] w-full flex-col overflow-hidden border-line/70 sm:min-h-0 sm:h-[min(900px,calc(100dvh-2.5rem))] sm:aspect-[9/19.5] sm:w-auto sm:rounded-[2rem] sm:border">
        <TopBar />
        <main className="w-full flex-1 overflow-y-auto px-5 pb-28 pt-6">
          <Outlet />
        </main>
        <BottomNav />
      </div>
    </div>
  );
}
