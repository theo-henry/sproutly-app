import { Navigate, Outlet, useLocation } from "react-router-dom";
import { Leaf } from "lucide-react";
import { useAuth } from "./AuthContext";

export default function ProtectedRoute() {
  const { session, loading } = useAuth();
  const location = useLocation();

  if (loading) {
    return (
      <div className="aurora-frame grid min-h-[100dvh] place-items-center px-6 text-ink">
        <div className="grid place-items-center gap-4 text-center">
          <span className="grid h-14 w-14 place-items-center rounded-2xl bg-leaf/12 text-leaf ring-1 ring-leaf/25">
            <Leaf className="h-6 w-6 animate-pulse" />
          </span>
          <p className="text-sm font-black uppercase tracking-[0.22em] text-charcoal">
            Loading Sproutly
          </p>
        </div>
      </div>
    );
  }

  if (!session) {
    return <Navigate to="/login" replace state={{ from: location }} />;
  }

  return <Outlet />;
}
