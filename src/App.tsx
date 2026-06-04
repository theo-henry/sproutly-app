// App router. The website is the web app experience itself; phone builds use
// the same core routes at a smaller viewport.
import { Routes, Route, Navigate } from "react-router-dom";
import Home from "./pages/Home";
import Products from "./pages/Products";
import MapPage from "./pages/MapPage";
import Recipes from "./pages/Recipes";
import MealPlan from "./pages/MealPlan";
import AppShell from "./components/layout/AppShell";

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<AppShell />}>
        <Route index element={<Home />} />
        <Route path="home" element={<Navigate to="/" replace />} />
        <Route path="products" element={<Products />} />
        <Route path="map" element={<MapPage />} />
        <Route path="recipes" element={<Recipes />} />
        <Route path="meal-plan" element={<MealPlan />} />
      </Route>

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
