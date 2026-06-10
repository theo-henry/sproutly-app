package com.sproutly.app.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocalDining
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
)

val bottomNavItems = listOf(
    BottomNavItem(Routes.HOME, "Home", Icons.Outlined.Home),
    BottomNavItem(Routes.PRODUCTS, "Products", Icons.Outlined.ShoppingBag),
    BottomNavItem(Routes.NEARBY, "Nearby", Icons.Outlined.Place),
    BottomNavItem(Routes.RECIPES, "Recipes", Icons.Outlined.LocalDining),
)
