// AppShell: responsive web app frame used by every route.
import { Outlet } from "react-router-dom";
import TopBar from "./TopBar";
import BottomNav from "./BottomNav";

export default function AppShell() {
  return (
    <div className="aurora-frame min-h-[100dvh] w-full px-0 text-ink sm:px-5">
      <div className="glass-frame mx-auto flex min-h-[100dvh] w-full max-w-7xl flex-col overflow-hidden border-line/70 sm:my-5 sm:min-h-[calc(100dvh-2.5rem)] sm:rounded-[1.75rem] sm:border">
        <TopBar />
        <main className="mx-auto w-full max-w-6xl flex-1 overflow-y-auto px-5 pb-28 pt-6 sm:px-8 lg:px-10">
          <Outlet />
        </main>
        <BottomNav />
      </div>
    </div>
  );
}
