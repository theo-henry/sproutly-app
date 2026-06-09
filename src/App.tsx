// App router. The website is the web app experience itself; phone builds use
// the same core routes at a smaller viewport.
import { Routes, Route, Navigate } from "react-router-dom";
import Home from "./pages/Home";
import Products from "./pages/Products";
import MapPage from "./pages/MapPage";
import Recipes from "./pages/Recipes";
import MealPlan from "./pages/MealPlan";
import Login from "./pages/Login";
import Account from "./pages/Account";
import AppShell from "./components/layout/AppShell";
import ProtectedRoute from "./auth/ProtectedRoute";

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />

      <Route element={<ProtectedRoute />}>
        <Route path="/" element={<AppShell />}>
          <Route index element={<Home />} />
          <Route path="home" element={<Navigate to="/" replace />} />
          <Route path="products" element={<Products />} />
          <Route path="map" element={<MapPage />} />
          <Route path="recipes" element={<Recipes />} />
          <Route path="meal-plan" element={<MealPlan />} />
          <Route path="account" element={<Account />} />
        </Route>
      </Route>

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
