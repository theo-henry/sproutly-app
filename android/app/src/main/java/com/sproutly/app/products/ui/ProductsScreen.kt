package com.sproutly.app.products.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import com.sproutly.app.products.ProductViewModel
import com.sproutly.app.products.ProductsUiState
import com.sproutly.app.products.model.CartSummary
import com.sproutly.app.products.model.DietLabel
import com.sproutly.app.products.model.Product
import com.sproutly.app.products.model.ProductCategory
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsScreen(
    onOpenScanner: () -> Unit,
    onOpenNearbyStore: (String?) -> Unit,
    viewModel: ProductViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        containerColor = BgDeep,
        // System bar insets are already consumed by the outer SignedInGraph
        // Scaffold (see AppNavGraph.consumeWindowInsets). Re-applying them here
        // would double-pad the content area, on some screens pushing the
        // bottom of the tappable region behind the system bottom nav bar.
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text("Products", fontWeight = FontWeight.SemiBold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BgDeep,
                    titleContentColor = TextPrimary,
                )
            )
        },
        bottomBar = {
            if (state.cartSummary.totalItems > 0) {
                CartSummaryBar(
                    summary = state.cartSummary,
                    onClear = viewModel::clearCart,
                    onFindStores = { onOpenNearbyStore(null) },
                )
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp)
                .padding(top = 4.dp, bottom = 34.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            FeaturedProduct(
                product = state.products.firstOrNull { it.featured },
                quantity = state.products.firstOrNull { it.featured }?.let { state.cartQuantities[it.id] ?: 0 } ?: 0,
                onAdd = { product -> viewModel.addToCart(product.id) },
                onFindNearby = { product -> onOpenNearbyStore(product.storeHints.firstOrNull()?.name) },
            )

            SearchAndFilters(
                state = state,
                onQueryChange = viewModel::updateQuery,
                onCategorySelected = viewModel::selectCategory,
            )

            ScannerCard(onOpenScanner)

            Row(verticalAlignment = Alignment.CenterVertically) {
                SectionLabel("Plant-based picks")
                Spacer(Modifier.weight(1f))
                Text(
                    "${state.visibleProducts.size} items",
                    color = TextMuted,
                    style = MaterialTheme.typography.labelMedium,
                )
            }

            if (state.visibleProducts.isEmpty() && !state.loading) {
                EmptyProducts()
            } else {
                state.visibleProducts.forEach { product ->
                    ProductCard(
                        product = product,
                        quantity = state.cartQuantities[product.id] ?: 0,
                        onAdd = { viewModel.addToCart(product.id) },
                        onDecrement = { viewModel.decrementCart(product.id) },
                        onRemove = { viewModel.removeFromCart(product.id) },
                        onFindNearby = { onOpenNearbyStore(product.storeHints.firstOrNull()?.name) },
                    )
                }
            }
        }
    }
}

@Composable
private fun FeaturedProduct(
    product: Product?,
    quantity: Int,
    onAdd: (Product) -> Unit,
    onFindNearby: (Product) -> Unit,
) {
    if (product == null) return

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(BgElevated)
            .border(1.dp, Divider, RoundedCornerShape(28.dp)),
    ) {
        ProductImage(
            product = product,
            modifier = Modifier.fillMaxSize(),
            fallbackLarge = true,
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, BgDeep.copy(alpha = 0.92f)),
                        startY = 90f,
                    )
                )
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(18.dp),
        ) {
            Badge("Featured pick")
            Spacer(Modifier.height(10.dp))
            Text(product.name, color = TextPrimary, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
            Text("${product.brand} · ${formatPrice(product.priceEur)} estimate", color = TextMuted, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MintPillButton(
                    label = if (quantity > 0) "Add another" else "Add to cart",
                    onClick = { onAdd(product) },
                    modifier = Modifier.weight(1f),
                )
                GhostButton(
                    label = "Nearby",
                    onClick = { onFindNearby(product) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun SearchAndFilters(
    state: ProductsUiState,
    onQueryChange: (String) -> Unit,
    onCategorySelected: (ProductCategory) -> Unit,
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
            placeholder = { Text("Search oat milk, tofu, snacks") },
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
            )
        )

        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            state.categories.forEach { category ->
                CategoryPill(
                    category = category,
                    selected = state.selectedCategory == category,
                    onClick = { onCategorySelected(category) },
                )
            }
        }
    }
}

@Composable
private fun ScannerCard(onOpenScanner: () -> Unit) {
    SproutlyCard(accent = true) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(Icons.Outlined.QrCodeScanner, contentDescription = null, tint = LeafMint)
            Column(modifier = Modifier.weight(1f)) {
                Text("Scan a label", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                Text("Use this later for barcode lookup and nutrition checks.", color = TextMuted, style = MaterialTheme.typography.bodyMedium)
            }
            GhostButton("Scan", onClick = onOpenScanner)
        }
    }
}

@Composable
private fun ProductCard(
    product: Product,
    quantity: Int,
    onAdd: () -> Unit,
    onDecrement: () -> Unit,
    onRemove: () -> Unit,
    onFindNearby: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = BgSurface,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Divider, RoundedCornerShape(24.dp)),
    ) {
        Column {
            Row(
                modifier = Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                ProductImage(
                    product = product,
                    modifier = Modifier
                        .width(112.dp)
                        .height(132.dp)
                        .clip(RoundedCornerShape(18.dp)),
                )
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Badge(product.dietLabel.label)
                        Spacer(Modifier.width(6.dp))
                        Text(
                            formatPrice(product.priceEur),
                            color = LeafMint,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Black,
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        product.name,
                        color = TextPrimary,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(product.brand, color = TextMuted, style = MaterialTheme.typography.bodyMedium, maxLines = 1)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        product.storeHints.firstOrNull()?.let { "Likely at ${it.name} · ${it.area}" } ?: "Check nearby supermarkets",
                        color = TextMuted,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(8.dp))
                    TagRow(product.tags.take(2))
                }
            }

            DividerLine()

            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                TextButton(
                    onClick = onFindNearby,
                    colors = ButtonDefaults.textButtonColors(contentColor = LeafMint),
                ) {
                    Icon(Icons.Outlined.Storefront, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Find nearby")
                }
                Spacer(Modifier.weight(1f))
                QuantityControl(
                    quantity = quantity,
                    onAdd = onAdd,
                    onDecrement = onDecrement,
                    onRemove = onRemove,
                )
            }
        }
    }
}

@Composable
private fun ProductImage(product: Product, modifier: Modifier = Modifier, fallbackLarge: Boolean = false) {
    Box(
        modifier = modifier
            .background(BgElevated),
        contentAlignment = Alignment.Center,
    ) {
        AsyncImage(
            model = product.imageUrl,
            contentDescription = product.name,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        Text(
            product.category.label,
            color = TextMuted.copy(alpha = if (fallbackLarge) 0.26f else 0.42f),
            style = if (fallbackLarge) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Black,
        )
    }
}

@Composable
private fun QuantityControl(
    quantity: Int,
    onAdd: () -> Unit,
    onDecrement: () -> Unit,
    onRemove: () -> Unit,
) {
    if (quantity <= 0) {
        Button(
            onClick = onAdd,
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(containerColor = LeafMint, contentColor = BgDeep),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
        ) {
            Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(4.dp))
            Text("Add", fontWeight = FontWeight.SemiBold)
        }
        return
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(BgElevated)
            .border(1.dp, Divider, RoundedCornerShape(50))
            .padding(3.dp),
    ) {
        IconButton(
            onClick = if (quantity == 1) onRemove else onDecrement,
            modifier = Modifier.size(32.dp),
        ) {
            Icon(Icons.Outlined.Remove, contentDescription = "Remove one", tint = TextPrimary, modifier = Modifier.size(17.dp))
        }
        Text(
            quantity.toString(),
            color = TextPrimary,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.widthIn(min = 24.dp),
        )
        IconButton(onClick = onAdd, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Outlined.Add, contentDescription = "Add one", tint = LeafMint, modifier = Modifier.size(17.dp))
        }
    }
}

@Composable
private fun CartSummaryBar(
    summary: CartSummary,
    onClear: () -> Unit,
    onFindStores: () -> Unit,
) {
    Surface(
        color = BgSurface,
        tonalElevation = 0.dp,
        shadowElevation = 12.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(LeafMint),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Outlined.ShoppingCart, contentDescription = null, tint = BgDeep)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "${summary.totalItems} items · ${formatPrice(summary.estimatedTotalEur)}",
                    color = TextPrimary,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text("Estimated cart, no checkout yet", color = TextMuted, style = MaterialTheme.typography.bodySmall)
            }
            TextButton(onClick = onClear, colors = ButtonDefaults.textButtonColors(contentColor = TextMuted)) {
                Text("Clear")
            }
            Button(
                onClick = onFindStores,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LeafMint, contentColor = BgDeep),
            ) {
                Text("Stores", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun CategoryPill(category: ProductCategory, selected: Boolean, onClick: () -> Unit) {
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
            category.label,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
        )
    }
}

@Composable
private fun Badge(label: String) {
    Surface(
        shape = RoundedCornerShape(50),
        color = LeafMint.copy(alpha = 0.16f),
        contentColor = LeafMint,
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun TagRow(tags: List<String>) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        tags.forEach { tag ->
            Surface(
                shape = RoundedCornerShape(50),
                color = BgElevated,
                contentColor = TextMuted,
            ) {
                Text(
                    tag,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}

@Composable
private fun EmptyProducts() {
    SproutlyCard {
        Text("No products match", color = TextPrimary, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(6.dp))
        Text("Try another category or search for a broader term like protein, snack, or dairy-free.", color = TextMuted)
    }
}

@Composable
private fun DividerLine() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Divider)
    )
}

private fun formatPrice(value: Double): String =
    "€${String.format(Locale.US, "%.2f", value)}"
