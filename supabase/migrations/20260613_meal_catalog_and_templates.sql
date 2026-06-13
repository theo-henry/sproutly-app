-- Curated meal catalog and weekly templates that power request-meal-plan.
-- The edge function picks one template matching the user's diet, then
-- resolves catalog ids to human-readable names.

create table if not exists public.meal_catalog (
  id text primary key,
  category text not null check (category in ('breakfast', 'lunch', 'dinner', 'snack')),
  name text not null,
  description text,
  diet_preferences text[] not null default '{}',
  diet_tags text[] not null default '{}',
  created_at timestamptz not null default now()
);

create index if not exists meal_catalog_category_idx
  on public.meal_catalog (category);

create table if not exists public.meal_plan_templates (
  id text primary key,
  name text not null,
  diet_preference text not null,
  tags text[] not null default '{}',
  slots jsonb not null,
  created_at timestamptz not null default now()
);

create index if not exists meal_plan_templates_diet_idx
  on public.meal_plan_templates (diet_preference);

alter table public.meal_catalog enable row level security;
alter table public.meal_plan_templates enable row level security;

drop policy if exists "Authenticated users can read meal catalog" on public.meal_catalog;
create policy "Authenticated users can read meal catalog"
on public.meal_catalog for select
to authenticated
using (true);

drop policy if exists "Authenticated users can read meal plan templates" on public.meal_plan_templates;
create policy "Authenticated users can read meal plan templates"
on public.meal_plan_templates for select
to authenticated
using (true);

-- All catalog meals are plant-based and therefore compatible with every
-- supported diet preference. Tags differentiate quick / high-protein /
-- budget / free-from variants and are used to bias template selection.
insert into public.meal_catalog (id, category, name, description, diet_preferences, diet_tags) values
  ('bf_tofu_scramble',   'breakfast', 'Tofu scramble with greens',            'Soft tofu, spinach, turmeric, toasted sourdough.', array['vegan','vegetarian','whole_food_plant_based','flexitarian','mostly_plant_based'], array['high-protein']),
  ('bf_overnight_oats',  'breakfast', 'Overnight oats with chia and berries', 'Rolled oats, chia, plant milk, mixed berries.',     array['vegan','vegetarian','whole_food_plant_based','flexitarian','mostly_plant_based'], array['quick-meals','budget-friendly']),
  ('bf_avocado_toast',   'breakfast', 'Avocado toast with hemp seeds',        'Sourdough, smashed avocado, hemp, lemon, chili.',   array['vegan','vegetarian','whole_food_plant_based','flexitarian','mostly_plant_based'], array['quick-meals']),
  ('bf_soy_yogurt_bowl', 'breakfast', 'Soy yogurt protein bowl',              'Soy yogurt, granola, banana, peanut butter.',       array['vegan','vegetarian','whole_food_plant_based','flexitarian','mostly_plant_based'], array['high-protein','quick-meals']),
  ('bf_pb_banana_oats',  'breakfast', 'Peanut butter banana oats',            'Stovetop oats, peanut butter, banana, cinnamon.',   array['vegan','vegetarian','whole_food_plant_based','flexitarian','mostly_plant_based'], array['budget-friendly','quick-meals']),
  ('bf_mushroom_wrap',   'breakfast', 'Mushroom breakfast wrap',              'Sauteed mushrooms, tofu, spinach, whole-wheat wrap.', array['vegan','vegetarian','whole_food_plant_based','flexitarian','mostly_plant_based'], array[]::text[]),
  ('bf_tempeh_hash',     'breakfast', 'Tempeh hash with potatoes',            'Crispy tempeh, potatoes, peppers, smoked paprika.',  array['vegan','vegetarian','whole_food_plant_based','flexitarian','mostly_plant_based'], array['high-protein']),
  ('bf_chia_pudding',    'breakfast', 'Chia pudding with mango',              'Chia, coconut milk, mango, lime zest.',              array['vegan','vegetarian','whole_food_plant_based','flexitarian','mostly_plant_based'], array['gluten-free','quick-meals']),

  ('ln_lentil_bowl',         'lunch', 'Lentil power bowl with brown rice',  'Brown lentils, brown rice, roasted veg, tahini.',  array['vegan','vegetarian','whole_food_plant_based','flexitarian','mostly_plant_based'], array['high-protein','budget-friendly']),
  ('ln_chickpea_wrap',       'lunch', 'Chickpea herb wrap',                 'Smashed chickpeas, herbs, lemon, lettuce, wrap.',  array['vegan','vegetarian','whole_food_plant_based','flexitarian','mostly_plant_based'], array['quick-meals']),
  ('ln_black_bean_burrito',  'lunch', 'Black bean burrito bowl',            'Black beans, rice, corn, salsa, avocado.',         array['vegan','vegetarian','whole_food_plant_based','flexitarian','mostly_plant_based'], array['budget-friendly','high-protein']),
  ('ln_quinoa_edamame',      'lunch', 'Quinoa edamame salad',               'Quinoa, edamame, cucumber, sesame-ginger dressing.', array['vegan','vegetarian','whole_food_plant_based','flexitarian','mostly_plant_based'], array['high-protein','gluten-free']),
  ('ln_sesame_tofu_noodles', 'lunch', 'Sesame tofu noodles',                'Tofu, soba, sesame sauce, scallions.',             array['vegan','vegetarian','whole_food_plant_based','flexitarian','mostly_plant_based'], array['high-protein']),
  ('ln_falafel_plate',       'lunch', 'Falafel plate with tabbouleh',       'Baked falafel, tabbouleh, hummus, pita.',          array['vegan','vegetarian','whole_food_plant_based','flexitarian','mostly_plant_based'], array[]::text[]),
  ('ln_white_bean_stew',     'lunch', 'White bean tomato stew',             'Cannellini, tomato, kale, garlic, olive oil.',     array['vegan','vegetarian','whole_food_plant_based','flexitarian','mostly_plant_based'], array['budget-friendly']),
  ('ln_peanut_soba',         'lunch', 'Peanut soba noodle salad',           'Cold soba, peanut sauce, carrots, cabbage.',       array['vegan','vegetarian','whole_food_plant_based','flexitarian','mostly_plant_based'], array['quick-meals']),

  ('dn_mushroom_tofu_stirfry', 'dinner', 'Mushroom tofu stir-fry',         'Tofu, mushrooms, broccoli, ginger, jasmine rice.', array['vegan','vegetarian','whole_food_plant_based','flexitarian','mostly_plant_based'], array['high-protein','quick-meals']),
  ('dn_tempeh_tacos',          'dinner', 'Smoky tempeh tacos',             'Crumbled tempeh, slaw, lime crema, corn tortillas.', array['vegan','vegetarian','whole_food_plant_based','flexitarian','mostly_plant_based'], array['high-protein']),
  ('dn_red_lentil_dal',        'dinner', 'Red lentil dal with quinoa',     'Red lentils, tomato, ginger, garlic, quinoa.',     array['vegan','vegetarian','whole_food_plant_based','flexitarian','mostly_plant_based'], array['high-protein','budget-friendly']),
  ('dn_chickpea_curry',        'dinner', 'Coconut chickpea curry',         'Chickpeas, coconut milk, spinach, basmati rice.',  array['vegan','vegetarian','whole_food_plant_based','flexitarian','mostly_plant_based'], array['budget-friendly']),
  ('dn_seitan_fajita',         'dinner', 'Seitan fajita bowl',             'Seitan, peppers, onions, lime rice, black beans.', array['vegan','vegetarian','flexitarian','mostly_plant_based'], array['high-protein']),
  ('dn_black_bean_chili',      'dinner', 'Black bean chili with avocado',  'Black beans, tomato, peppers, cumin, avocado.',    array['vegan','vegetarian','whole_food_plant_based','flexitarian','mostly_plant_based'], array['budget-friendly','high-protein']),
  ('dn_cashew_pasta',          'dinner', 'Cashew greens pasta',            'Cashew cream, kale, lemon, whole-wheat pasta.',    array['vegan','vegetarian','whole_food_plant_based','flexitarian','mostly_plant_based'], array[]::text[]),
  ('dn_squash_risotto',        'dinner', 'Roasted squash risotto',         'Arborio, roasted butternut, sage, nutritional yeast.', array['vegan','vegetarian','whole_food_plant_based','flexitarian','mostly_plant_based'], array['gluten-free']),

  ('sn_hummus_carrots',    'snack', 'Hummus with carrots',         'Classic hummus, carrot sticks.',          array['vegan','vegetarian','whole_food_plant_based','flexitarian','mostly_plant_based'], array['quick-meals','budget-friendly']),
  ('sn_edamame',           'snack', 'Roasted edamame',             'Sea-salted roasted edamame.',             array['vegan','vegetarian','whole_food_plant_based','flexitarian','mostly_plant_based'], array['high-protein','quick-meals']),
  ('sn_smoothie',          'snack', 'Protein smoothie',            'Pea protein, banana, berries, plant milk.', array['vegan','vegetarian','whole_food_plant_based','flexitarian','mostly_plant_based'], array['high-protein','quick-meals']),
  ('sn_trail_mix',         'snack', 'Trail mix',                   'Almonds, walnuts, raisins, dark chocolate.', array['vegan','vegetarian','whole_food_plant_based','flexitarian','mostly_plant_based'], array['quick-meals']),
  ('sn_apple_pb',          'snack', 'Apple with peanut butter',    'Crisp apple slices, peanut butter.',      array['vegan','vegetarian','whole_food_plant_based','flexitarian','mostly_plant_based'], array['quick-meals','budget-friendly']),
  ('sn_chocolate_yogurt',  'snack', 'Dark chocolate soy yogurt',   'Soy yogurt, cacao nibs, almonds.',        array['vegan','vegetarian','whole_food_plant_based','flexitarian','mostly_plant_based'], array['quick-meals']),
  ('sn_fruit_seeds',       'snack', 'Fruit with pumpkin seeds',    'Seasonal fruit, toasted pumpkin seeds.',  array['vegan','vegetarian','whole_food_plant_based','flexitarian','mostly_plant_based'], array['quick-meals','gluten-free']),
  ('sn_rice_cakes',        'snack', 'Rice cakes with almond butter', 'Rice cakes, almond butter, banana slices.', array['vegan','vegetarian','whole_food_plant_based','flexitarian','mostly_plant_based'], array['quick-meals'])
on conflict (id) do update set
  category = excluded.category,
  name = excluded.name,
  description = excluded.description,
  diet_preferences = excluded.diet_preferences,
  diet_tags = excluded.diet_tags;

-- 12 curated weekly templates (3 per main diet). Each slots array has 7
-- catalog ids — one per day — so the resulting week has coherent variety.
insert into public.meal_plan_templates (id, name, diet_preference, tags, slots) values
  ('tpl_vegan_classic', 'Vegan classic week', 'vegan', array[]::text[], jsonb_build_object(
    'breakfast', jsonb_build_array('bf_tofu_scramble','bf_overnight_oats','bf_avocado_toast','bf_mushroom_wrap','bf_pb_banana_oats','bf_soy_yogurt_bowl','bf_chia_pudding'),
    'lunch',     jsonb_build_array('ln_lentil_bowl','ln_chickpea_wrap','ln_quinoa_edamame','ln_falafel_plate','ln_sesame_tofu_noodles','ln_white_bean_stew','ln_peanut_soba'),
    'dinner',    jsonb_build_array('dn_cashew_pasta','dn_red_lentil_dal','dn_mushroom_tofu_stirfry','dn_tempeh_tacos','dn_chickpea_curry','dn_squash_risotto','dn_black_bean_chili'),
    'snack',     jsonb_build_array('sn_hummus_carrots','sn_edamame','sn_smoothie','sn_trail_mix','sn_apple_pb','sn_chocolate_yogurt','sn_fruit_seeds')
  )),
  ('tpl_vegan_high_protein', 'Vegan high-protein week', 'vegan', array['high-protein'], jsonb_build_object(
    'breakfast', jsonb_build_array('bf_tofu_scramble','bf_soy_yogurt_bowl','bf_tempeh_hash','bf_pb_banana_oats','bf_tofu_scramble','bf_soy_yogurt_bowl','bf_tempeh_hash'),
    'lunch',     jsonb_build_array('ln_lentil_bowl','ln_quinoa_edamame','ln_sesame_tofu_noodles','ln_black_bean_burrito','ln_lentil_bowl','ln_sesame_tofu_noodles','ln_quinoa_edamame'),
    'dinner',    jsonb_build_array('dn_mushroom_tofu_stirfry','dn_tempeh_tacos','dn_red_lentil_dal','dn_seitan_fajita','dn_black_bean_chili','dn_mushroom_tofu_stirfry','dn_tempeh_tacos'),
    'snack',     jsonb_build_array('sn_smoothie','sn_edamame','sn_smoothie','sn_apple_pb','sn_edamame','sn_smoothie','sn_chocolate_yogurt')
  )),
  ('tpl_vegan_quick', 'Vegan quick & easy week', 'vegan', array['quick-meals'], jsonb_build_object(
    'breakfast', jsonb_build_array('bf_overnight_oats','bf_avocado_toast','bf_soy_yogurt_bowl','bf_pb_banana_oats','bf_chia_pudding','bf_overnight_oats','bf_avocado_toast'),
    'lunch',     jsonb_build_array('ln_chickpea_wrap','ln_peanut_soba','ln_black_bean_burrito','ln_chickpea_wrap','ln_quinoa_edamame','ln_peanut_soba','ln_lentil_bowl'),
    'dinner',    jsonb_build_array('dn_mushroom_tofu_stirfry','dn_chickpea_curry','dn_cashew_pasta','dn_tempeh_tacos','dn_red_lentil_dal','dn_black_bean_chili','dn_mushroom_tofu_stirfry'),
    'snack',     jsonb_build_array('sn_hummus_carrots','sn_apple_pb','sn_trail_mix','sn_rice_cakes','sn_fruit_seeds','sn_chocolate_yogurt','sn_smoothie')
  )),
  ('tpl_vegetarian_classic', 'Vegetarian classic week', 'vegetarian', array[]::text[], jsonb_build_object(
    'breakfast', jsonb_build_array('bf_soy_yogurt_bowl','bf_avocado_toast','bf_overnight_oats','bf_mushroom_wrap','bf_pb_banana_oats','bf_tofu_scramble','bf_chia_pudding'),
    'lunch',     jsonb_build_array('ln_falafel_plate','ln_chickpea_wrap','ln_white_bean_stew','ln_lentil_bowl','ln_quinoa_edamame','ln_peanut_soba','ln_black_bean_burrito'),
    'dinner',    jsonb_build_array('dn_squash_risotto','dn_cashew_pasta','dn_chickpea_curry','dn_mushroom_tofu_stirfry','dn_red_lentil_dal','dn_tempeh_tacos','dn_black_bean_chili'),
    'snack',     jsonb_build_array('sn_hummus_carrots','sn_fruit_seeds','sn_chocolate_yogurt','sn_apple_pb','sn_trail_mix','sn_rice_cakes','sn_edamame')
  )),
  ('tpl_vegetarian_high_protein', 'Vegetarian high-protein week', 'vegetarian', array['high-protein'], jsonb_build_object(
    'breakfast', jsonb_build_array('bf_soy_yogurt_bowl','bf_tofu_scramble','bf_tempeh_hash','bf_pb_banana_oats','bf_soy_yogurt_bowl','bf_tofu_scramble','bf_tempeh_hash'),
    'lunch',     jsonb_build_array('ln_quinoa_edamame','ln_lentil_bowl','ln_sesame_tofu_noodles','ln_black_bean_burrito','ln_quinoa_edamame','ln_lentil_bowl','ln_sesame_tofu_noodles'),
    'dinner',    jsonb_build_array('dn_tempeh_tacos','dn_red_lentil_dal','dn_mushroom_tofu_stirfry','dn_black_bean_chili','dn_seitan_fajita','dn_tempeh_tacos','dn_red_lentil_dal'),
    'snack',     jsonb_build_array('sn_smoothie','sn_edamame','sn_chocolate_yogurt','sn_smoothie','sn_edamame','sn_apple_pb','sn_smoothie')
  )),
  ('tpl_vegetarian_quick', 'Vegetarian quick & easy week', 'vegetarian', array['quick-meals'], jsonb_build_object(
    'breakfast', jsonb_build_array('bf_avocado_toast','bf_overnight_oats','bf_chia_pudding','bf_pb_banana_oats','bf_soy_yogurt_bowl','bf_avocado_toast','bf_overnight_oats'),
    'lunch',     jsonb_build_array('ln_chickpea_wrap','ln_peanut_soba','ln_black_bean_burrito','ln_quinoa_edamame','ln_chickpea_wrap','ln_white_bean_stew','ln_falafel_plate'),
    'dinner',    jsonb_build_array('dn_mushroom_tofu_stirfry','dn_chickpea_curry','dn_cashew_pasta','dn_squash_risotto','dn_black_bean_chili','dn_red_lentil_dal','dn_tempeh_tacos'),
    'snack',     jsonb_build_array('sn_hummus_carrots','sn_apple_pb','sn_rice_cakes','sn_trail_mix','sn_fruit_seeds','sn_chocolate_yogurt','sn_smoothie')
  )),
  ('tpl_wfpb_classic', 'Whole-food plant-based classic', 'whole_food_plant_based', array[]::text[], jsonb_build_object(
    'breakfast', jsonb_build_array('bf_overnight_oats','bf_chia_pudding','bf_pb_banana_oats','bf_tofu_scramble','bf_mushroom_wrap','bf_overnight_oats','bf_tempeh_hash'),
    'lunch',     jsonb_build_array('ln_lentil_bowl','ln_white_bean_stew','ln_quinoa_edamame','ln_black_bean_burrito','ln_lentil_bowl','ln_falafel_plate','ln_white_bean_stew'),
    'dinner',    jsonb_build_array('dn_red_lentil_dal','dn_chickpea_curry','dn_black_bean_chili','dn_squash_risotto','dn_red_lentil_dal','dn_mushroom_tofu_stirfry','dn_chickpea_curry'),
    'snack',     jsonb_build_array('sn_hummus_carrots','sn_fruit_seeds','sn_apple_pb','sn_edamame','sn_trail_mix','sn_fruit_seeds','sn_apple_pb')
  )),
  ('tpl_wfpb_high_protein', 'Whole-food plant-based high-protein', 'whole_food_plant_based', array['high-protein'], jsonb_build_object(
    'breakfast', jsonb_build_array('bf_tofu_scramble','bf_tempeh_hash','bf_pb_banana_oats','bf_tofu_scramble','bf_tempeh_hash','bf_soy_yogurt_bowl','bf_tofu_scramble'),
    'lunch',     jsonb_build_array('ln_lentil_bowl','ln_quinoa_edamame','ln_black_bean_burrito','ln_lentil_bowl','ln_quinoa_edamame','ln_black_bean_burrito','ln_lentil_bowl'),
    'dinner',    jsonb_build_array('dn_red_lentil_dal','dn_black_bean_chili','dn_mushroom_tofu_stirfry','dn_tempeh_tacos','dn_red_lentil_dal','dn_black_bean_chili','dn_mushroom_tofu_stirfry'),
    'snack',     jsonb_build_array('sn_edamame','sn_smoothie','sn_apple_pb','sn_edamame','sn_smoothie','sn_fruit_seeds','sn_edamame')
  )),
  ('tpl_wfpb_budget', 'Whole-food plant-based on a budget', 'whole_food_plant_based', array['budget-friendly'], jsonb_build_object(
    'breakfast', jsonb_build_array('bf_overnight_oats','bf_pb_banana_oats','bf_overnight_oats','bf_pb_banana_oats','bf_tofu_scramble','bf_overnight_oats','bf_pb_banana_oats'),
    'lunch',     jsonb_build_array('ln_lentil_bowl','ln_white_bean_stew','ln_black_bean_burrito','ln_lentil_bowl','ln_white_bean_stew','ln_black_bean_burrito','ln_lentil_bowl'),
    'dinner',    jsonb_build_array('dn_red_lentil_dal','dn_chickpea_curry','dn_black_bean_chili','dn_red_lentil_dal','dn_chickpea_curry','dn_black_bean_chili','dn_red_lentil_dal'),
    'snack',     jsonb_build_array('sn_hummus_carrots','sn_apple_pb','sn_hummus_carrots','sn_apple_pb','sn_fruit_seeds','sn_hummus_carrots','sn_apple_pb')
  )),
  ('tpl_flexitarian_classic', 'Flexitarian classic week', 'flexitarian', array[]::text[], jsonb_build_object(
    'breakfast', jsonb_build_array('bf_avocado_toast','bf_overnight_oats','bf_soy_yogurt_bowl','bf_mushroom_wrap','bf_tofu_scramble','bf_pb_banana_oats','bf_chia_pudding'),
    'lunch',     jsonb_build_array('ln_chickpea_wrap','ln_falafel_plate','ln_quinoa_edamame','ln_black_bean_burrito','ln_peanut_soba','ln_lentil_bowl','ln_white_bean_stew'),
    'dinner',    jsonb_build_array('dn_cashew_pasta','dn_mushroom_tofu_stirfry','dn_chickpea_curry','dn_tempeh_tacos','dn_squash_risotto','dn_red_lentil_dal','dn_black_bean_chili'),
    'snack',     jsonb_build_array('sn_trail_mix','sn_apple_pb','sn_hummus_carrots','sn_chocolate_yogurt','sn_fruit_seeds','sn_edamame','sn_smoothie')
  )),
  ('tpl_flexitarian_high_protein', 'Flexitarian high-protein week', 'flexitarian', array['high-protein'], jsonb_build_object(
    'breakfast', jsonb_build_array('bf_tofu_scramble','bf_soy_yogurt_bowl','bf_tempeh_hash','bf_pb_banana_oats','bf_tofu_scramble','bf_soy_yogurt_bowl','bf_tempeh_hash'),
    'lunch',     jsonb_build_array('ln_lentil_bowl','ln_sesame_tofu_noodles','ln_quinoa_edamame','ln_black_bean_burrito','ln_lentil_bowl','ln_sesame_tofu_noodles','ln_quinoa_edamame'),
    'dinner',    jsonb_build_array('dn_seitan_fajita','dn_tempeh_tacos','dn_mushroom_tofu_stirfry','dn_red_lentil_dal','dn_black_bean_chili','dn_seitan_fajita','dn_tempeh_tacos'),
    'snack',     jsonb_build_array('sn_smoothie','sn_edamame','sn_apple_pb','sn_smoothie','sn_edamame','sn_chocolate_yogurt','sn_smoothie')
  )),
  ('tpl_flexitarian_quick', 'Flexitarian quick & easy week', 'flexitarian', array['quick-meals'], jsonb_build_object(
    'breakfast', jsonb_build_array('bf_avocado_toast','bf_overnight_oats','bf_chia_pudding','bf_soy_yogurt_bowl','bf_pb_banana_oats','bf_avocado_toast','bf_overnight_oats'),
    'lunch',     jsonb_build_array('ln_chickpea_wrap','ln_peanut_soba','ln_black_bean_burrito','ln_quinoa_edamame','ln_chickpea_wrap','ln_peanut_soba','ln_falafel_plate'),
    'dinner',    jsonb_build_array('dn_mushroom_tofu_stirfry','dn_chickpea_curry','dn_cashew_pasta','dn_tempeh_tacos','dn_red_lentil_dal','dn_black_bean_chili','dn_mushroom_tofu_stirfry'),
    'snack',     jsonb_build_array('sn_apple_pb','sn_rice_cakes','sn_hummus_carrots','sn_trail_mix','sn_fruit_seeds','sn_chocolate_yogurt','sn_smoothie')
  ))
on conflict (id) do update set
  name = excluded.name,
  diet_preference = excluded.diet_preference,
  tags = excluded.tags,
  slots = excluded.slots;
