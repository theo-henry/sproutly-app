package com.sproutly.app.recipes.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sproutly.app.core.design.*
import com.sproutly.app.recipes.RecipeViewModel
import com.sproutly.app.recipes.model.Recipe

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipesScreen(
    onOpenMealPlan: () -> Unit,
    viewModel: RecipeViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        containerColor = BgDeep,
        topBar = {
            TopAppBar(
                title = { Text("Recipes") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgDeep, titleContentColor = TextPrimary)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            MintPillButton("Open AI meal plan", onClick = onOpenMealPlan, modifier = Modifier.fillMaxWidth())

            state.featured?.let { featured ->
                SproutlyCard(accent = true) {
                    SectionLabel("Tonight")
                    Spacer(Modifier.height(8.dp))
                    Text(featured.title, style = MaterialTheme.typography.titleLarge, color = TextPrimary)
                    featured.subtitle?.let { Text(it, color = TextMuted) }
                }
            }

            SectionLabel("Quick & easy")
            state.quick.forEach { RecipeRow(it) }

            SectionLabel("Seasonal picks")
            state.seasonal.forEach { RecipeRow(it) }

            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun RecipeRow(recipe: Recipe) {
    SproutlyCard {
        Text(recipe.title, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
        val subtitle = listOfNotNull(
            recipe.prepMinutes?.let { "$it min" },
            recipe.proteinGrams?.let { "${it}g protein" },
        ).joinToString(" · ")
        if (subtitle.isNotBlank()) {
            Text(subtitle, color = TextMuted, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
