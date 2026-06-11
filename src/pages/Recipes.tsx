import { useMemo, useState } from "react";
import { Link } from "react-router-dom";
import {
  Bookmark,
  CalendarPlus,
  ChefHat,
  Clock,
  Dumbbell,
  Flame,
  Leaf,
  ListChecks,
  Search,
  Utensils,
  Users,
  X,
} from "lucide-react";
import { recipes, RECIPE_FILTERS, type RecipeFilter } from "../data/recipes";
import type { Recipe } from "../types";
import SectionHeader from "../components/ui/SectionHeader";
import { Interactive, PageTitle, Reveal, Stagger, StaggerItem } from "../components/ui/Motion";

const featuredRecipe = recipes.find((recipe) => recipe.id === "smoky-tempeh-tacos") ?? recipes[0];

function recipeMatchesFilter(recipe: Recipe, filter: RecipeFilter) {
  if (filter === "All") return true;
  return recipe.dietLabels.includes(filter as Recipe["dietLabels"][number]) || recipe.tags.includes(filter);
}

function RecipeImage({
  recipe,
  className = "",
}: {
  recipe: Recipe;
  className?: string;
}) {
  const [failed, setFailed] = useState(false);

  return (
    <div
      className={`relative overflow-hidden bg-[radial-gradient(circle_at_35%_15%,oklch(0.74_0.12_142_/_0.5),transparent_34%),linear-gradient(135deg,var(--color-panel-soft),var(--color-canopy))] ${className}`}
    >
      {recipe.imageUrl && !failed ? (
        <img
          src={recipe.imageUrl}
          alt={recipe.title}
          className="h-full w-full object-cover"
          loading="lazy"
          onError={() => setFailed(true)}
        />
      ) : (
        <div className="flex h-full w-full items-center justify-center">
          <Leaf className="h-10 w-10 text-mint/70" aria-hidden="true" />
        </div>
      )}
      <div className="pointer-events-none absolute inset-0 bg-gradient-to-t from-void/62 via-transparent to-transparent" />
    </div>
  );
}

function StatPill({
  icon: Icon,
  label,
}: {
  icon: typeof Clock;
  label: string;
}) {
  return (
    <span className="inline-flex items-center gap-1.5 rounded-full border border-mint/15 bg-void/45 px-2.5 py-1 text-[0.68rem] font-black text-mint">
      <Icon className="h-3.5 w-3.5" aria-hidden="true" />
      {label}
    </span>
  );
}

function RecipeCard({
  recipe,
  onOpen,
}: {
  recipe: Recipe;
  onOpen: (recipe: Recipe) => void;
}) {
  return (
    <Interactive>
      <button
        type="button"
        onClick={() => onOpen(recipe)}
        className="group w-full overflow-hidden rounded-2xl border border-line/70 bg-panel-soft/58 text-left shadow-[0_24px_90px_-58px_black] transition hover:border-leaf/45"
      >
        <RecipeImage recipe={recipe} className="aspect-[16/10] w-full" />
        <div className="p-4">
          <div className="mb-3 flex flex-wrap gap-1.5">
            {recipe.dietLabels.slice(0, 1).map((label) => (
              <span
                key={label}
                className="rounded-full bg-leaf/14 px-2 py-1 text-[0.62rem] font-black uppercase tracking-[0.16em] text-leaf"
              >
                {label}
              </span>
            ))}
            {recipe.tags.slice(0, 2).map((tag) => (
              <span
                key={tag}
                className="rounded-full bg-mint/8 px-2 py-1 text-[0.62rem] font-black uppercase tracking-[0.16em] text-charcoal"
              >
                {tag}
              </span>
            ))}
          </div>
          <h3 className="text-lg font-black leading-tight text-ink transition group-hover:text-mint">
            {recipe.title}
          </h3>
          <div className="mt-3 flex flex-wrap gap-2">
            <StatPill icon={Clock} label={`${recipe.minutes} min`} />
            <StatPill icon={Dumbbell} label={`${recipe.proteinGrams}g protein`} />
            <StatPill icon={Flame} label={`${recipe.kcal} kcal`} />
          </div>
        </div>
      </button>
    </Interactive>
  );
}

function DetailMetric({
  label,
  value,
}: {
  label: string;
  value: string;
}) {
  return (
    <div className="rounded-2xl border border-line/65 bg-void/28 p-3">
      <div className="text-[0.62rem] font-black uppercase tracking-[0.18em] text-leaf/75">
        {label}
      </div>
      <div className="mt-1 text-base font-black text-ink">{value}</div>
    </div>
  );
}

function RecipeDetail({
  recipe,
  onClose,
}: {
  recipe: Recipe;
  onClose: () => void;
}) {
  return (
    <div className="fixed inset-0 z-50 flex items-end justify-center bg-black/68 px-0 sm:p-5">
      <button
        type="button"
        aria-label="Close recipe details"
        className="absolute inset-0 cursor-default"
        onClick={onClose}
      />
      <section
        role="dialog"
        aria-modal="true"
        aria-labelledby="recipe-detail-title"
        className="relative max-h-[92dvh] w-full overflow-y-auto rounded-t-[1.6rem] border border-line/70 bg-canopy shadow-[0_-30px_120px_-42px_black] sm:max-w-[430px] sm:rounded-[1.8rem]"
      >
        <RecipeImage recipe={recipe} className="h-56 w-full" />
        <button
          type="button"
          aria-label="Close"
          onClick={onClose}
          className="absolute right-4 top-4 inline-flex h-10 w-10 items-center justify-center rounded-full border border-mint/20 bg-void/70 text-mint backdrop-blur transition hover:bg-leaf/20"
        >
          <X className="h-5 w-5" aria-hidden="true" />
        </button>

        <div className="space-y-6 p-5">
          <div>
            <div className="flex flex-wrap gap-2">
              {[...recipe.dietLabels, ...recipe.tags.slice(0, 2)].map((tag) => (
                <span
                  key={tag}
                  className="rounded-full border border-leaf/20 bg-leaf/10 px-2.5 py-1 text-[0.65rem] font-black uppercase tracking-[0.16em] text-leaf"
                >
                  {tag}
                </span>
              ))}
            </div>
            <h2 id="recipe-detail-title" className="mt-4 text-3xl font-black leading-none text-ink">
              {recipe.title}
            </h2>
            <p className="mt-3 text-sm leading-6 text-charcoal">{recipe.description}</p>
          </div>

          <div className="grid grid-cols-2 gap-2">
            <DetailMetric label="Total" value={`${recipe.minutes} min`} />
            <DetailMetric label="Serves" value={`${recipe.servings}`} />
            <DetailMetric label="Prep" value={`${recipe.prepMinutes} min`} />
            <DetailMetric label="Cook" value={`${recipe.cookMinutes} min`} />
          </div>

          <div className="grid grid-cols-5 gap-2">
            <DetailMetric label="Kcal" value={`${recipe.kcal}`} />
            <DetailMetric label="Protein" value={`${recipe.proteinGrams}g`} />
            <DetailMetric label="Carbs" value={`${recipe.carbsGrams}g`} />
            <DetailMetric label="Fat" value={`${recipe.fatGrams}g`} />
            <DetailMetric label="Fiber" value={`${recipe.fiberGrams}g`} />
          </div>

          <div className="grid gap-3">
            <button
              type="button"
              className="inline-flex items-center justify-center gap-2 rounded-full bg-leaf px-5 py-3 text-xs font-black uppercase tracking-[0.16em] text-void transition hover:bg-mint"
            >
              <CalendarPlus className="h-4 w-4" aria-hidden="true" />
              Add to plan
            </button>
            <button
              type="button"
              className="inline-flex items-center justify-center gap-2 rounded-full border border-line/70 bg-panel-soft/65 px-5 py-3 text-xs font-black uppercase tracking-[0.16em] text-mint transition hover:border-leaf/45 hover:bg-leaf/12"
            >
              <Bookmark className="h-4 w-4" aria-hidden="true" />
              Save recipe
            </button>
          </div>

          <section>
            <h3 className="flex items-center gap-2 text-lg font-black text-ink">
              <Utensils className="h-5 w-5 text-leaf" aria-hidden="true" />
              Ingredients
            </h3>
            <ul className="mt-3 grid gap-2">
              {recipe.ingredients.map((ingredient) => (
                <li
                  key={ingredient}
                  className="rounded-xl border border-line/55 bg-panel-soft/40 px-3 py-2 text-sm leading-5 text-charcoal"
                >
                  {ingredient}
                </li>
              ))}
            </ul>
          </section>

          <section>
            <h3 className="flex items-center gap-2 text-lg font-black text-ink">
              <ListChecks className="h-5 w-5 text-leaf" aria-hidden="true" />
              Preparation
            </h3>
            <ol className="mt-3 grid gap-3">
              {recipe.steps.map((step, index) => (
                <li key={step} className="grid grid-cols-[2rem_1fr] gap-3 text-sm leading-6 text-charcoal">
                  <span className="flex h-8 w-8 items-center justify-center rounded-full bg-leaf text-xs font-black text-void">
                    {index + 1}
                  </span>
                  <span className="pt-1">{step}</span>
                </li>
              ))}
            </ol>
          </section>

          <section className="grid gap-3 pb-2">
            <div className="rounded-2xl border border-line/60 bg-panel-soft/42 p-4">
              <h3 className="text-sm font-black text-ink">Equipment</h3>
              <p className="mt-2 text-sm leading-6 text-charcoal">{recipe.equipment.join(", ")}</p>
            </div>
            <div className="rounded-2xl border border-line/60 bg-panel-soft/42 p-4">
              <h3 className="text-sm font-black text-ink">Allergens</h3>
              <p className="mt-2 text-sm leading-6 text-charcoal">
                {recipe.allergens.length ? recipe.allergens.join(", ") : "No common allergens listed"}
              </p>
            </div>
          </section>
        </div>
      </section>
    </div>
  );
}

export default function Recipes() {
  const [selectedFilter, setSelectedFilter] = useState<RecipeFilter>("All");
  const [query, setQuery] = useState("");
  const [selectedRecipe, setSelectedRecipe] = useState<Recipe | null>(null);

  const filteredRecipes = useMemo(() => {
    const normalizedQuery = query.trim().toLowerCase();

    return recipes.filter((recipe) => {
      const matchesFilter = recipeMatchesFilter(recipe, selectedFilter);
      const searchable = [
        recipe.title,
        recipe.description,
        recipe.mealType,
        recipe.difficulty,
        ...recipe.tags,
        ...recipe.dietLabels,
        ...recipe.ingredients,
      ]
        .join(" ")
        .toLowerCase();

      return matchesFilter && (!normalizedQuery || searchable.includes(normalizedQuery));
    });
  }, [query, selectedFilter]);

  const quickRecipes = useMemo(
    () => recipes.filter((recipe) => recipe.minutes <= 20).slice(0, 4),
    [],
  );

  return (
    <div className="space-y-8">
      <PageTitle
        eyebrow="Recipes"
        title="Cook from a sharper plant-based library."
        body="Find recipes by diet, timing, pantry needs, and macros, then open the full method when one fits."
        action={
          <Link
            to="/meal-plan"
            className="inline-flex items-center justify-center gap-2 rounded-full bg-leaf px-5 py-3 text-xs font-black uppercase tracking-[0.16em] text-void transition hover:bg-mint"
          >
            <ChefHat className="h-4 w-4" aria-hidden="true" />
            AI meal plan
          </Link>
        }
      />

      <Reveal mode="scale">
        <button
          type="button"
          onClick={() => setSelectedRecipe(featuredRecipe)}
          className="group relative min-h-72 w-full overflow-hidden rounded-[1.6rem] border border-leaf/35 bg-panel-soft text-left shadow-[0_30px_100px_-60px_black]"
        >
          <RecipeImage recipe={featuredRecipe} className="absolute inset-0 h-full w-full" />
          <div className="absolute inset-0 bg-gradient-to-t from-void via-void/48 to-transparent" />
          <div className="relative flex min-h-72 flex-col justify-end p-5">
            <p className="text-xs font-black uppercase tracking-[0.24em] text-leaf">
              Tonight
            </p>
            <h2 className="mt-3 text-3xl font-black leading-none text-ink transition group-hover:text-mint">
              {featuredRecipe.title}
            </h2>
            <p className="mt-3 line-clamp-2 text-sm leading-6 text-charcoal">
              {featuredRecipe.description}
            </p>
            <div className="mt-4 flex flex-wrap gap-2">
              <StatPill icon={Clock} label={`${featuredRecipe.minutes} min`} />
              <StatPill icon={Users} label={`${featuredRecipe.servings} servings`} />
              <StatPill icon={Dumbbell} label={`${featuredRecipe.proteinGrams}g protein`} />
            </div>
          </div>
        </button>
      </Reveal>

      <Reveal mode="drift">
        <div className="grid gap-3">
          <label className="relative block">
            <Search
              className="pointer-events-none absolute left-4 top-1/2 h-4 w-4 -translate-y-1/2 text-leaf"
              aria-hidden="true"
            />
            <input
              value={query}
              onChange={(event) => setQuery(event.target.value)}
              placeholder="Search lentils, quick meals, gluten-free..."
              className="h-12 w-full rounded-full border border-line/70 bg-panel-soft/70 pl-11 pr-4 text-sm font-bold text-ink outline-none transition placeholder:text-charcoal/70 focus:border-leaf/55 focus:bg-panel-soft"
            />
          </label>

          <div className="no-scrollbar -mx-5 flex gap-2 overflow-x-auto px-5">
            {RECIPE_FILTERS.map((filter) => {
              const selected = selectedFilter === filter;
              return (
                <button
                  key={filter}
                  type="button"
                  onClick={() => setSelectedFilter(filter)}
                  className={`shrink-0 rounded-full border px-4 py-2 text-xs font-black transition ${
                    selected
                      ? "border-leaf bg-leaf text-void"
                      : "border-line/70 bg-panel-soft/70 text-charcoal hover:border-leaf/40 hover:bg-leaf/12 hover:text-mint"
                  }`}
                >
                  {filter}
                </button>
              );
            })}
          </div>
        </div>
      </Reveal>

      <section>
        <SectionHeader title={`${filteredRecipes.length} matching recipes`} />
        {filteredRecipes.length ? (
          <Stagger className="grid gap-4">
            {filteredRecipes.map((recipe, index) => (
              <StaggerItem key={recipe.id} mode={index % 2 === 0 ? "rise" : "scale"}>
                <RecipeCard recipe={recipe} onOpen={setSelectedRecipe} />
              </StaggerItem>
            ))}
          </Stagger>
        ) : (
          <Reveal mode="soft">
            <div className="rounded-2xl border border-line/70 bg-panel-soft/55 p-5 text-sm leading-6 text-charcoal">
              No recipes match this search yet. Try a broader term like protein, quick, lentils, or vegan.
            </div>
          </Reveal>
        )}
      </section>

      <section>
        <SectionHeader title="Fastest picks" />
        <Stagger className="grid gap-3">
          {quickRecipes.map((recipe) => (
            <StaggerItem key={recipe.id} mode="drift">
              <button
                type="button"
                onClick={() => setSelectedRecipe(recipe)}
                className="grid w-full grid-cols-[5rem_1fr] gap-3 rounded-2xl border border-line/65 bg-panel-soft/45 p-2 text-left transition hover:border-leaf/40 hover:bg-leaf/10"
              >
                <RecipeImage recipe={recipe} className="aspect-square rounded-xl" />
                <div className="min-w-0 py-1 pr-2">
                  <h3 className="truncate text-sm font-black text-ink">{recipe.title}</h3>
                  <p className="mt-1 text-xs leading-5 text-charcoal">
                    {recipe.minutes} min · {recipe.proteinGrams}g protein · {recipe.mealType}
                  </p>
                  <p className="mt-1 truncate text-[0.68rem] font-black uppercase tracking-[0.14em] text-leaf/80">
                    {recipe.dietLabels[0]}
                  </p>
                </div>
              </button>
            </StaggerItem>
          ))}
        </Stagger>
      </section>

      {selectedRecipe ? (
        <RecipeDetail recipe={selectedRecipe} onClose={() => setSelectedRecipe(null)} />
      ) : null}
    </div>
  );
}
