// AppShell: responsive web app frame used by every route.
import { Outlet } from "react-router-dom";
import TopBar from "./TopBar";
import BottomNav from "./BottomNav";

export default function AppShell() {
  return (
    <div className="surface-grid min-h-[100dvh] w-full bg-mist">
      <div className="mx-auto flex min-h-[100dvh] w-full max-w-6xl flex-col bg-paper shadow-[0_30px_100px_-70px_black] sm:my-6 sm:min-h-[calc(100dvh-3rem)] sm:rounded-2xl sm:ring-1 sm:ring-ink/10">
        <TopBar />
        <main className="mx-auto w-full max-w-5xl flex-1 overflow-y-auto px-5 pb-28 pt-5 sm:px-8">
          <Outlet />
        </main>
        <BottomNav />
      </div>
    </div>
  );
}
