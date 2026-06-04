// App router. Mobile-first: every route renders inside a phone-sized
// frame on desktop, fullscreen on phones.
import { Routes, Route, Navigate } from "react-router-dom";
import Landing from "./pages/Landing";
import Home from "./pages/Home";
import Products from "./pages/Products";
import MapPage from "./pages/MapPage";
import Recipes from "./pages/Recipes";
import MealPlan from "./pages/MealPlan";
import AppShell from "./components/layout/AppShell";

export default function App() {
  return (
    <Routes>
      {/* Public landing — no bottom nav, fullscreen storytelling. */}
      <Route path="/" element={<Landing />} />

      {/* Authenticated/app routes share the mobile shell (top bar + bottom tab nav). */}
      <Route element={<AppShell />}>
        <Route path="/home" element={<Home />} />
        <Route path="/products" element={<Products />} />
        <Route path="/map" element={<MapPage />} />
        <Route path="/recipes" element={<Recipes />} />
        <Route path="/meal-plan" element={<MealPlan />} />
      </Route>

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
