package com.sproutly.app.home.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.ArrowOutward
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.sproutly.app.core.design.*
import com.sproutly.app.core.result.UiState
import com.sproutly.app.mealplan.MealPlanViewModel
import com.sproutly.app.mealplan.model.MealPlan
import com.sproutly.app.mealplan.model.MealSlot
import com.sproutly.app.products.ProductViewModel
import com.sproutly.app.recipes.RecipeViewModel
import com.sproutly.app.recipes.model.Recipe
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onOpenAccount: () -> Unit,
    onOpenScanner: () -> Unit,
    onOpenNearby: () -> Unit,
    onOpenProducts: () -> Unit = {},
    onOpenRecipes: () -> Unit = {},
    onOpenMealPlan: () -> Unit = {},
    mealPlanViewModel: MealPlanViewModel = viewModel(),
    recipeViewModel: RecipeViewModel = viewModel(),
    productViewModel: ProductViewModel = viewModel(),
) {
    val mealState by mealPlanViewModel.state.collectAsState()
    val recipeState by recipeViewModel.state.collectAsState()
    val productState by productViewModel.state.collectAsState()

    val today = remember { LocalDate.now() }
    val now = remember { LocalTime.now() }
    val greeting = remember(now) { greetingFor(now) }
    val plan = (mealState as? UiState.Success<MealPlan>)?.data
    val todayMeals = remember(plan, today) { mealsForToday(plan, today) }

    val featuredCount = remember(productState.products) { productState.products.count { it.featured } }
    val quickCount = recipeState.quickRecipes.size

    Scaffold(
        containerColor = BgDeep,
        topBar = {
            HomeTopBar(onOpenAccount = onOpenAccount)
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 4.dp, bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp),
        ) {
            Reveal(delayMillis = 0) {
                EditorialGreeting(greeting = greeting, date = today)
            }
            Reveal(delayMillis = 70) {
                TodaysPlateCard(
                    meals = todayMeals,
                    hasPlan = plan != null && plan.days.any { it.meals.isNotEmpty() },
                    onOpenPlan = onOpenMealPlan,
                )
            }
            Reveal(delayMillis = 210) {
                QuickActions(
                    onOpenScanner = onOpenScanner,
                    onOpenNearby = onOpenNearby,
                    onOpenMealPlan = onOpenMealPlan,
                )
            }
            Reveal(delayMillis = 280) {
                recipeState.featured?.let { recipe ->
                    RecipeOfDay(recipe = recipe, onOpen = onOpenRecipes)
                }
            }
            Reveal(delayMillis = 350) {
                DiscoverRow(
                    featuredProducts = featuredCount,
                    quickRecipes = quickCount,
                    onOpenProducts = onOpenProducts,
                    onOpenNearby = onOpenNearby,
                )
            }
        }
    }
}

// region — Header

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeTopBar(onOpenAccount: () -> Unit) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(LeafMint)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = "sproutly",
                    color = TextPrimary,
                    fontWeight = FontWeight.Medium,
                    fontSize = 19.sp,
                    letterSpacing = (-0.4).sp,
                )
            }
        },
        actions = {
            IconButton(onClick = onOpenAccount) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(BgSurface)
                        .border(1.dp, Divider, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Outlined.AccountCircle,
                        contentDescription = "Account",
                        tint = LeafMint,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }
            Spacer(Modifier.width(6.dp))
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = BgDeep,
            titleContentColor = TextPrimary,
        ),
    )
}

// endregion

// region — Editorial greeting

@Composable
private fun EditorialGreeting(greeting: String, date: LocalDate) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = date.format(DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.ENGLISH)).uppercase(),
            color = TextMuted,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            letterSpacing = 1.6.sp,
        )
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = greeting + ".",
                color = TextPrimary,
                fontWeight = FontWeight.Normal,
                fontStyle = FontStyle.Italic,
                fontSize = 34.sp,
                lineHeight = 38.sp,
                letterSpacing = (-0.8).sp,
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Here's what your plant-based day looks like.",
            color = TextMuted,
            fontSize = 14.sp,
            lineHeight = 20.sp,
        )
    }
}

// endregion

// region — Today's plate

@Composable
private fun TodaysPlateCard(
    meals: List<Pair<MealSlot, String>>,
    hasPlan: Boolean,
    onOpenPlan: () -> Unit,
) {
    val shape = RoundedCornerShape(26.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(BgSurface)
            .border(1.dp, Divider, shape)
            .drawBehind {
                val r = size.minDimension * 0.7f
                drawCircleGlow(
                    center = Offset(size.width * 0.92f, -r * 0.4f),
                    radius = r,
                    color = LeafGreen.copy(alpha = 0.18f),
                )
                drawCircleGlow(
                    center = Offset(size.width * 0.05f, size.height * 1.05f),
                    radius = r * 0.55f,
                    color = LeafMint.copy(alpha = 0.10f),
                )
            }
            .padding(22.dp),
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(LeafMint)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "TODAY ON YOUR PLATE",
                    color = LeafMint,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 2.sp,
                )
                Spacer(Modifier.weight(1f))
                Icon(
                    Icons.Outlined.CalendarMonth,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onOpenPlan)
                        .padding(4.dp),
                )
            }

            Spacer(Modifier.height(16.dp))

            if (meals.isEmpty()) {
                Text(
                    "No meals planned yet.",
                    color = TextPrimary,
                    fontStyle = FontStyle.Italic,
                    fontSize = 22.sp,
                    lineHeight = 28.sp,
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    "Generate a starter week or build your own — it'll appear here every morning.",
                    color = TextMuted,
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                )
                Spacer(Modifier.height(14.dp))
                Row {
                    PlateButton(
                        label = if (hasPlan) "Open meal plan" else "Build my week",
                        onClick = onOpenPlan,
                    )
                }
            } else {
                meals.forEachIndexed { index, (slot, dish) ->
                    PlateRow(slot = slot, dish = dish, isLast = index == meals.lastIndex)
                }
                Spacer(Modifier.height(14.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Tap the calendar to edit your week",
                        color = TextMuted,
                        fontSize = 12.sp,
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        "Plan →",
                        color = LeafMint,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable(onClick = onOpenPlan)
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun PlateRow(slot: MealSlot, dish: String, isLast: Boolean) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(LeafMint.copy(alpha = 0.95f))
                    .border(2.dp, BgSurface, CircleShape),
            )
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .width(1.dp)
                        .height(46.dp)
                        .background(Divider)
                )
            }
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.padding(bottom = if (isLast) 0.dp else 16.dp)) {
            Text(
                slot.label.uppercase(),
                color = TextMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.4.sp,
            )
            Spacer(Modifier.height(3.dp))
            Text(
                dish.ifBlank { "—" },
                color = TextPrimary,
                fontSize = 18.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight.Normal,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun PlateButton(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(LeafMint)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 12.dp),
    ) {
        Text(label, color = BgDeep, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
    }
}

// endregion

// region — Quick actions

@Composable
private fun QuickActions(
    onOpenScanner: () -> Unit,
    onOpenNearby: () -> Unit,
    onOpenMealPlan: () -> Unit,
) {
    Column {
        SectionHeader(label = "Quick actions")
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickActionTile(
                modifier = Modifier.weight(1f),
                label = "Scan",
                hint = "label / barcode",
                icon = Icons.Outlined.QrCodeScanner,
                onClick = onOpenScanner,
            )
            QuickActionTile(
                modifier = Modifier.weight(1f),
                label = "Map",
                hint = "spots near you",
                icon = Icons.Outlined.Place,
                onClick = onOpenNearby,
            )
            QuickActionTile(
                modifier = Modifier.weight(1f),
                label = "Plan",
                hint = "AI meal week",
                icon = Icons.Outlined.Restaurant,
                onClick = onOpenMealPlan,
            )
        }
    }
}

@Composable
private fun QuickActionTile(
    modifier: Modifier = Modifier,
    label: String,
    hint: String,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(20.dp)
    Column(
        modifier = modifier
            .clip(shape)
            .background(BgSurface)
            .border(1.dp, Divider, shape)
            .clickable(onClick = onClick)
            .padding(14.dp),
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        listOf(LeafMint.copy(alpha = 0.22f), LeafGreen.copy(alpha = 0.08f))
                    )
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = LeafMint, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.height(14.dp))
        Text(label, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        Text(hint, color = TextMuted, fontSize = 11.sp)
    }
}

// endregion

// region — Recipe of the day

@Composable
private fun RecipeOfDay(recipe: Recipe, onOpen: () -> Unit) {
    val shape = RoundedCornerShape(24.dp)
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SectionHeader(label = "Today's recipe", trailing = "from your feed")
        }
        Spacer(Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(shape)
                .background(BgElevated)
                .border(1.dp, Divider, shape)
                .clickable(onClick = onOpen),
        ) {
            val img = recipe.imageResId
            if (img != null) {
                AsyncImage(
                    model = img,
                    contentDescription = recipe.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            } else if (recipe.imageUrl != null) {
                AsyncImage(
                    model = recipe.imageUrl,
                    contentDescription = recipe.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawWithContent {
                        drawContent()
                        drawRect(
                            brush = Brush.verticalGradient(
                                0f to Color.Transparent,
                                0.35f to Color.Transparent,
                                1f to Color(0xCC000000),
                            )
                        )
                    }
            )

            // top-left badge
            Row(
                modifier = Modifier
                    .padding(14.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color(0x66000000))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(LeafMint)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    "RECIPE OF THE DAY",
                    color = Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.6.sp,
                )
            }

            // bottom info
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(18.dp)
                    .fillMaxWidth(),
            ) {
                Text(
                    recipe.title,
                    color = Color.White,
                    fontSize = 22.sp,
                    lineHeight = 26.sp,
                    fontWeight = FontWeight.Medium,
                )
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    MetaChip("${recipe.totalMinutes} min")
                    Spacer(Modifier.width(8.dp))
                    MetaChip("${recipe.macros.proteinGrams}g protein")
                    Spacer(Modifier.width(8.dp))
                    MetaChip(recipe.difficulty)
                    Spacer(Modifier.weight(1f))
                    Icon(
                        Icons.Outlined.ArrowOutward,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(LeafMint.copy(alpha = 0.9f))
                            .padding(8.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun MetaChip(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(Color(0x55000000))
            .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(text, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}

// endregion

// region — Discover row

@Composable
private fun DiscoverRow(
    featuredProducts: Int,
    quickRecipes: Int,
    onOpenProducts: () -> Unit,
    onOpenNearby: () -> Unit,
) {
    Column {
        SectionHeader(label = "Worth a look")
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            DiscoverTile(
                modifier = Modifier.weight(1f),
                eyebrow = "DEALS",
                title = if (featuredProducts > 0) "$featuredProducts featured finds" else "Featured finds",
                hint = "Plant-based shelves",
                icon = Icons.Outlined.ShoppingBag,
                onClick = onOpenProducts,
            )
            DiscoverTile(
                modifier = Modifier.weight(1f),
                eyebrow = "NEARBY",
                title = "Plant-forward spots",
                hint = "Restaurants & shops",
                icon = Icons.Outlined.Place,
                onClick = onOpenNearby,
            )
        }
    }
}

@Composable
private fun DiscoverTile(
    modifier: Modifier = Modifier,
    eyebrow: String,
    title: String,
    hint: String,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(22.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .background(BgSurface)
            .border(1.dp, Divider, shape)
            .clickable(onClick = onClick)
            .drawBehind {
                val r = size.minDimension * 0.9f
                drawCircleGlow(
                    center = Offset(size.width * 1.05f, size.height * 1.1f),
                    radius = r * 0.7f,
                    color = LeafGreen.copy(alpha = 0.14f),
                )
            }
            .padding(16.dp),
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(eyebrow, color = LeafMint, fontSize = 10.sp, letterSpacing = 1.8.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.weight(1f))
                Icon(icon, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
            }
            Spacer(Modifier.height(38.dp))
            Text(
                title,
                color = TextPrimary,
                fontSize = 17.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight.Medium,
            )
            Spacer(Modifier.height(2.dp))
            Text(hint, color = TextMuted, fontSize = 12.sp)
        }
    }
}

// endregion

// region — Reusable

@Composable
private fun SectionHeader(label: String, trailing: String? = null) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            label,
            color = TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
        )
        if (trailing != null) {
            Spacer(Modifier.width(10.dp))
            Text(trailing, color = TextMuted, fontSize = 12.sp, fontStyle = FontStyle.Italic)
        }
    }
}

/**
 * Single load-time reveal: staggered fade + slight slide-up. Mirrors editorial
 * reveals from the rest of the app without depending on Motion library.
 */
@Composable
private fun Reveal(delayMillis: Int, content: @Composable () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(delayMillis.toLong())
        visible = true
    }
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 520),
        label = "reveal-alpha",
    )
    val translate by animateFloatAsState(
        targetValue = if (visible) 0f else 18f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "reveal-translate",
    )
    Box(
        modifier = Modifier
            .alpha(alpha)
            .offset { IntOffset(0, translate.roundToInt()) }
    ) {
        content()
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCircleGlow(
    center: Offset,
    radius: Float,
    color: Color,
) {
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(color, Color.Transparent),
            center = center,
            radius = radius,
        ),
        radius = radius,
        center = center,
    )
}

// endregion

// region — Data helpers

private fun greetingFor(now: LocalTime): String = when (now.hour) {
    in 5..11 -> "Good morning"
    in 12..16 -> "Good afternoon"
    in 17..21 -> "Good evening"
    else -> "Still up"
}

private fun mealsForToday(plan: MealPlan?, today: LocalDate): List<Pair<MealSlot, String>> {
    val day = plan?.days?.firstOrNull { it.date == today.toString() } ?: return emptyList()
    return listOf(MealSlot.BREAKFAST, MealSlot.LUNCH, MealSlot.DINNER)
        .mapNotNull { slot ->
            val text = day.meal(slot)
            if (text.isBlank()) null else slot to text
        }
}

// endregion
