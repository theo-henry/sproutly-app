package com.sproutly.app.recipes.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.sproutly.app.core.design.BgDeep
import com.sproutly.app.core.design.BgElevated
import com.sproutly.app.core.design.BgSurface
import com.sproutly.app.core.design.Divider
import com.sproutly.app.core.design.GhostButton
import com.sproutly.app.core.design.LeafMint
import com.sproutly.app.core.design.MintPillButton
import com.sproutly.app.core.design.SectionLabel
import com.sproutly.app.core.design.SproutlyCard
import com.sproutly.app.core.design.TextMuted
import com.sproutly.app.core.design.TextPrimary
import com.sproutly.app.recipes.RecipeViewModel
import com.sproutly.app.recipes.RecipesUiState
import com.sproutly.app.recipes.model.Recipe
import com.sproutly.app.recipes.model.RecipeFilter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipesScreen(
    onOpenMealPlan: () -> Unit,
    viewModel: RecipeViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        containerColor = BgDeep,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text("Recipes", fontWeight = FontWeight.SemiBold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BgDeep,
                    titleContentColor = TextPrimary,
                ),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp)
                .padding(top = 4.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            MintPillButton(
                label = "Open AI meal plan",
                onClick = onOpenMealPlan,
                modifier = Modifier.fillMaxWidth(),
            )

            state.featured?.let { recipe ->
                FeaturedRecipe(recipe = recipe, onClick = { viewModel.openRecipe(recipe) })
            }

            SearchAndFilters(
                state = state,
                onQueryChange = viewModel::updateQuery,
                onFilterSelected = viewModel::selectFilter,
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                SectionLabel("${state.visibleRecipes.size} matching recipes")
                Spacer(Modifier.weight(1f))
                Text("Tap for details", color = TextMuted, style = MaterialTheme.typography.labelMedium)
            }

            if (state.visibleRecipes.isEmpty()) {
                EmptyRecipes()
            } else {
                state.visibleRecipes.forEach { recipe ->
                    RecipeCard(
                        recipe = recipe,
                        saved = recipe.id in state.savedRecipeIds,
                        onClick = { viewModel.openRecipe(recipe) },
                    )
                }
            }

            SectionLabel("Fastest picks")
            state.quickRecipes.forEach { recipe ->
                QuickRecipeRow(recipe = recipe, onClick = { viewModel.openRecipe(recipe) })
            }
        }
    }

    state.selectedRecipe?.let { recipe ->
        RecipeDetailSheet(
            recipe = recipe,
            saved = recipe.id in state.savedRecipeIds,
            onDismiss = viewModel::closeRecipe,
            onToggleSaved = { viewModel.toggleSaved(recipe) },
            onOpenMealPlan = {
                viewModel.closeRecipe()
                onOpenMealPlan()
            },
        )
    }
}

@Composable
private fun FeaturedRecipe(recipe: Recipe, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(BgElevated)
            .border(1.dp, LeafMint.copy(alpha = 0.35f), RoundedCornerShape(28.dp))
            .clickable(onClick = onClick),
    ) {
        RecipeImage(recipe = recipe, modifier = Modifier.fillMaxSize(), fallbackLabel = "Tonight")
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, BgDeep.copy(alpha = 0.95f)),
                        startY = 96f,
                    )
                )
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(18.dp),
        ) {
            Badge("Tonight")
            Spacer(Modifier.height(10.dp))
            Text(
                recipe.title,
                color = TextPrimary,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
            )
            Text(
                recipe.description,
                color = TextMuted,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatChip(Icons.Outlined.AccessTime, "${recipe.totalMinutes} min")
                StatChip(Icons.Outlined.People, "${recipe.servings} servings")
            }
        }
    }
}

@Composable
private fun SearchAndFilters(
    state: RecipesUiState,
    onQueryChange: (String) -> Unit,
    onFilterSelected: (RecipeFilter) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = state.query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
            trailingIcon = {
                if (state.query.isNotBlank()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(Icons.Outlined.Close, contentDescription = "Clear search")
                    }
                }
            },
            placeholder = { Text("Search lentils, quick meals, gluten-free") },
            shape = RoundedCornerShape(18.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedContainerColor = BgSurface,
                unfocusedContainerColor = BgSurface,
                focusedBorderColor = LeafMint,
                unfocusedBorderColor = Divider,
                focusedLeadingIconColor = LeafMint,
                unfocusedLeadingIconColor = TextMuted,
                focusedTrailingIconColor = TextMuted,
                unfocusedTrailingIconColor = TextMuted,
                focusedPlaceholderColor = TextMuted,
                unfocusedPlaceholderColor = TextMuted,
            ),
        )

        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            state.filters.forEach { filter ->
                FilterPill(
                    filter = filter,
                    selected = state.selectedFilter == filter,
                    onClick = { onFilterSelected(filter) },
                )
            }
        }
    }
}

@Composable
private fun RecipeCard(recipe: Recipe, saved: Boolean, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = BgSurface,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Divider, RoundedCornerShape(24.dp))
            .clickable(onClick = onClick),
    ) {
        Column {
            RecipeImage(
                recipe = recipe,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp),
                fallbackLabel = recipe.mealType,
            )
            Column(modifier = Modifier.padding(14.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Badge(recipe.dietLabels.first().label)
                    recipe.tags.take(1).forEach { Badge(it, muted = true) }
                    if (saved) Badge("Saved", muted = true)
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    recipe.title,
                    color = TextPrimary,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                )
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatChip(Icons.Outlined.AccessTime, "${recipe.totalMinutes} min")
                    StatChip(Icons.Outlined.FitnessCenter, "${recipe.macros.proteinGrams}g protein")
                    StatChip(Icons.Outlined.LocalFireDepartment, "${recipe.macros.calories} kcal")
                }
            }
        }
    }
}

@Composable
private fun QuickRecipeRow(recipe: Recipe, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = BgSurface,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Divider, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RecipeImage(
                recipe = recipe,
                modifier = Modifier
                    .size(76.dp)
                    .clip(RoundedCornerShape(16.dp)),
                fallbackLabel = recipe.mealType,
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    recipe.title,
                    color = TextPrimary,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    "${recipe.totalMinutes} min · ${recipe.macros.proteinGrams}g protein · ${recipe.mealType}",
                    color = TextMuted,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    recipe.dietLabels.first().label,
                    color = LeafMint,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecipeDetailSheet(
    recipe: Recipe,
    saved: Boolean,
    onDismiss: () -> Unit,
    onToggleSaved: () -> Unit,
    onOpenMealPlan: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = BgDeep,
        contentColor = TextPrimary,
        dragHandle = { BottomSheetDefaults.DragHandle(color = TextMuted) },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp)
                .padding(bottom = 34.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(BgElevated)
                    .border(1.dp, Divider, RoundedCornerShape(26.dp)),
            ) {
                RecipeImage(recipe = recipe, modifier = Modifier.fillMaxSize(), fallbackLabel = recipe.mealType)
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                        .clip(RoundedCornerShape(50))
                        .background(BgDeep.copy(alpha = 0.76f)),
                ) {
                    Icon(Icons.Outlined.Close, contentDescription = "Close", tint = LeafMint)
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                recipe.dietLabels.take(2).forEach { Badge(it.label) }
                recipe.tags.take(1).forEach { Badge(it, muted = true) }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    recipe.title,
                    color = TextPrimary,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                )
                Text(recipe.description, color = TextMuted, style = MaterialTheme.typography.bodyMedium)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricTile("Total", "${recipe.totalMinutes} min", Modifier.weight(1f))
                MetricTile("Serves", recipe.servings.toString(), Modifier.weight(1f))
                MetricTile("Level", recipe.difficulty, Modifier.weight(1f))
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricTile("Kcal", recipe.macros.calories.toString(), Modifier.weight(1f))
                MetricTile("Protein", "${recipe.macros.proteinGrams}g", Modifier.weight(1f))
                MetricTile("Carbs", "${recipe.macros.carbsGrams}g", Modifier.weight(1f))
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricTile("Fat", "${recipe.macros.fatGrams}g", Modifier.weight(1f))
                MetricTile("Fiber", "${recipe.macros.fiberGrams}g", Modifier.weight(1f))
                MetricTile("Prep", "${recipe.prepMinutes} min", Modifier.weight(1f))
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MintPillButton("Add to plan", onClick = onOpenMealPlan, modifier = Modifier.weight(1f))
                GhostButton(if (saved) "Saved" else "Save", onClick = onToggleSaved, modifier = Modifier.weight(1f))
            }

            DetailSection(
                title = "Ingredients",
                icon = Icons.Outlined.Restaurant,
            ) {
                recipe.ingredients.forEach { ingredient ->
                    DetailListItem(text = ingredient)
                }
            }

            DetailSection(
                title = "Preparation",
                icon = Icons.Outlined.CheckCircleOutline,
            ) {
                recipe.steps.forEachIndexed { index, step ->
                    StepItem(index = index + 1, text = step)
                }
            }

            InfoCard("Equipment", recipe.equipment.joinToString(", "))
            InfoCard(
                "Allergens",
                recipe.allergens.takeIf { it.isNotEmpty() }?.joinToString(", ") ?: "No common allergens listed",
            )
        }
    }
}

@Composable
private fun RecipeImage(recipe: Recipe, modifier: Modifier = Modifier, fallbackLabel: String) {
    val imageModel = recipe.imageResId ?: recipe.imageUrl

    Box(
        modifier = modifier.background(BgElevated),
        contentAlignment = Alignment.Center,
    ) {
        if (imageModel != null) {
            AsyncImage(
                model = imageModel,
                contentDescription = recipe.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        } else {
            Text(
                fallbackLabel,
                color = TextMuted.copy(alpha = 0.36f),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
            )
        }
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, BgDeep.copy(alpha = 0.32f)),
                    )
                )
        )
    }
}

@Composable
private fun StatChip(icon: ImageVector, label: String) {
    Surface(
        shape = RoundedCornerShape(50),
        color = BgDeep.copy(alpha = 0.58f),
        contentColor = LeafMint,
        modifier = Modifier.border(1.dp, LeafMint.copy(alpha = 0.16f), RoundedCornerShape(50)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun FilterPill(filter: RecipeFilter, selected: Boolean, onClick: () -> Unit) {
    val bg = if (selected) LeafMint else BgSurface
    val fg = if (selected) BgDeep else TextPrimary
    Surface(
        shape = RoundedCornerShape(50),
        color = bg,
        contentColor = fg,
        modifier = Modifier
            .border(1.dp, if (selected) LeafMint else Divider, RoundedCornerShape(50))
            .clickable(onClick = onClick),
    ) {
        Text(
            filter.label,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
        )
    }
}

@Composable
private fun Badge(label: String, muted: Boolean = false) {
    Surface(
        shape = RoundedCornerShape(50),
        color = if (muted) BgElevated else LeafMint.copy(alpha = 0.16f),
        contentColor = if (muted) TextMuted else LeafMint,
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun MetricTile(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(BgSurface)
            .border(1.dp, Divider, RoundedCornerShape(16.dp))
            .padding(10.dp),
    ) {
        Text(label.uppercase(), color = TextMuted, style = MaterialTheme.typography.labelSmall)
        Text(
            value,
            color = TextPrimary,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Black,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun DetailSection(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(icon, contentDescription = null, tint = LeafMint)
            Text(title, color = TextPrimary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp), content = content)
    }
}

@Composable
private fun DetailListItem(text: String) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = BgSurface,
        modifier = Modifier.fillMaxWidth().border(1.dp, Divider, RoundedCornerShape(14.dp)),
    ) {
        Text(
            text,
            color = TextMuted,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
        )
    }
}

@Composable
private fun StepItem(index: Int, text: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(RoundedCornerShape(50))
                .background(LeafMint),
            contentAlignment = Alignment.Center,
        ) {
            Text(index.toString(), color = BgDeep, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Black)
        }
        Text(
            text,
            color = TextMuted,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 4.dp).weight(1f),
        )
    }
}

@Composable
private fun InfoCard(title: String, body: String) {
    SproutlyCard {
        Text(title, color = TextPrimary, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(6.dp))
        Text(body, color = TextMuted, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun EmptyRecipes() {
    SproutlyCard {
        Text("No recipes match", color = TextPrimary, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(6.dp))
        Text("Try a broader term like protein, quick, lentils, gluten-free, or vegan.", color = TextMuted)
    }
}
