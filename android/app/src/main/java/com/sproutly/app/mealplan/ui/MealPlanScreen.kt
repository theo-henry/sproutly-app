package com.sproutly.app.mealplan.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sproutly.app.core.design.*
import com.sproutly.app.core.result.UiState
import com.sproutly.app.mealplan.MealPlanViewModel
import com.sproutly.app.mealplan.model.MealSlot
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealPlanScreen(
    onBack: () -> Unit,
    viewModel: MealPlanViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsState()
    val message by viewModel.message.collectAsState()
    val generating by viewModel.generating.collectAsState()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(message) {
        message?.let { snackbar.showSnackbar(it); viewModel.clearMessage() }
    }

    Scaffold(
        containerColor = BgDeep,
        topBar = {
            TopAppBar(
                title = { Text("Meal plan") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBack, contentDescription = "Back") }
                },
                actions = {
                    IconButton(onClick = viewModel::requestGeneratedPlan, enabled = !generating) {
                        Icon(Icons.Outlined.AutoAwesome, contentDescription = "Generate", tint = LeafMint)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BgDeep, titleContentColor = TextPrimary, navigationIconContentColor = TextPrimary
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        when (val s = state) {
            UiState.Loading -> Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                CircularProgressIndicator(color = LeafMint)
            }
            is UiState.Error -> Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text(s.message, color = TextMuted)
            }
            UiState.Empty -> Box(Modifier.padding(padding).fillMaxSize())
            is UiState.Success -> {
                val plan = s.data
                LazyColumn(
                    modifier = Modifier.padding(padding).fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item {
                        Text(
                            "Week of ${formatWeek(plan.weekStartISO)}",
                            color = TextMuted,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                    item {
                        SproutlyCard(accent = true) {
                            Text(
                                "Generate and email plan",
                                color = TextPrimary,
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Uses your saved diet preference and tags, saves the latest plan to your account, and emails it through the connected Google Apps Script workflow.",
                                color = TextMuted,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            Spacer(Modifier.height(14.dp))
                            MintPillButton(
                                label = if (generating) "Generating..." else "Generate a Meal Plan",
                                onClick = viewModel::requestGeneratedPlan,
                                enabled = !generating,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                    items(plan.days.withIndex().toList()) { (idx, day) ->
                        SproutlyCard {
                            Text(formatDay(day.date), style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                            Spacer(Modifier.height(10.dp))
                            MealSlot.entries.forEach { slot ->
                                OutlinedTextField(
                                    value = day.meal(slot),
                                    onValueChange = { viewModel.updateMeal(idx, slot, it) },
                                    label = { Text(slot.label) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = LeafMint,
                                        unfocusedBorderColor = Divider,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary,
                                        focusedLabelColor = LeafMint,
                                        unfocusedLabelColor = TextMuted,
                                        cursorColor = LeafMint,
                                    )
                                )
                                Spacer(Modifier.height(6.dp))
                            }
                        }
                    }
                    item {
                        MintPillButton("Save week", onClick = viewModel::save, modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(40.dp))
                    }
                }
            }
        }
    }
}

private fun formatWeek(iso: String): String =
    runCatching { LocalDate.parse(iso).format(DateTimeFormatter.ofPattern("MMM d")) }.getOrDefault(iso)

private fun formatDay(iso: String): String =
    runCatching { LocalDate.parse(iso).format(DateTimeFormatter.ofPattern("EEEE d MMM")) }.getOrDefault(iso)
