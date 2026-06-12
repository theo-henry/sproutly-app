package com.sproutly.app.navigation

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.sproutly.app.auth.AuthState
import com.sproutly.app.auth.AuthViewModel
import com.sproutly.app.auth.ui.LoginScreen
import com.sproutly.app.core.design.BgDeep
import com.sproutly.app.core.design.BgSurface
import com.sproutly.app.core.design.LeafMint
import com.sproutly.app.core.design.TextMuted
import com.sproutly.app.home.ui.HomeScreen
import com.sproutly.app.mealplan.ui.MealPlanScreen
import com.sproutly.app.nearby.ui.NearbyScreen
import com.sproutly.app.products.ui.ProductsScreen
import com.sproutly.app.profile.ui.AccountScreen
import com.sproutly.app.recipes.ui.RecipesScreen
import com.sproutly.app.scanner.ui.ScannerScreen

@Composable
fun AppNavGraph(authState: AuthState, authViewModel: AuthViewModel) {
    when (authState) {
        AuthState.Loading -> SplashLoading()
        AuthState.SignedOut -> LoginScreen(viewModel = authViewModel)
        is AuthState.SignedIn -> SignedInGraph(onSignOut = { authViewModel.signOut() })
    }
}

@Composable
private fun SplashLoading() {
    Surface(color = BgDeep) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = LeafMint)
        }
    }
}

/**
 * Nearby destination accepts two optional URL args so we don't have to hoist
 * mutable state into SignedInGraph (state hoisted up here would cause the
 * entire signed-in tree — and every destination composable — to recompose any
 * time we pass a cross-screen hint, which made click lambdas race against
 * NavHost re-evaluation).
 */
private const val NEARBY_ROUTE =
    "${Routes.NEARBY}?fromProducts={fromProducts}&storeHint={storeHint}"

private fun nearbyDeepLink(fromProducts: Boolean, storeHint: String?): String {
    val encoded = storeHint?.let { Uri.encode(it) }.orEmpty()
    return "${Routes.NEARBY}?fromProducts=$fromProducts&storeHint=$encoded"
}

@Composable
private fun SignedInGraph(onSignOut: () -> Unit) {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    val showBottomBar = currentRoute in bottomNavItems.map { it.route } ||
        currentRoute?.startsWith(Routes.NEARBY) == true

    Scaffold(
        containerColor = BgDeep,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(containerColor = BgSurface, tonalElevation = 0.dp) {
                    bottomNavItems.forEach { item ->
                        val selected = currentRoute?.startsWith(item.route) == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                val target = if (item.route == Routes.NEARBY) {
                                    nearbyDeepLink(fromProducts = false, storeHint = null)
                                } else item.route
                                navController.navigate(target) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = false
                                    }
                                    launchSingleTop = true
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = LeafMint,
                                selectedTextColor = LeafMint,
                                indicatorColor = BgDeep,
                                unselectedIconColor = TextMuted,
                                unselectedTextColor = TextMuted,
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier
                .padding(padding)
                .consumeWindowInsets(padding)
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    onOpenAccount = { navController.navigate(Routes.ACCOUNT) },
                    onOpenScanner = { navController.navigate(Routes.SCANNER) },
                    onOpenNearby = {
                        navController.navigate(nearbyDeepLink(false, null))
                    },
                    onOpenProducts = {
                        navController.navigate(Routes.PRODUCTS) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = false }
                            launchSingleTop = true
                        }
                    },
                    onOpenRecipes = {
                        navController.navigate(Routes.RECIPES) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = false }
                            launchSingleTop = true
                        }
                    },
                    onOpenMealPlan = { navController.navigate(Routes.MEAL_PLAN) },
                )
            }
            composable(Routes.PRODUCTS) {
                ProductsScreen(
                    onOpenScanner = { navController.navigate(Routes.SCANNER) },
                    onOpenNearbyStore = { storeName ->
                        navController.navigate(nearbyDeepLink(true, storeName))
                    },
                )
            }
            composable(
                route = NEARBY_ROUTE,
                arguments = listOf(
                    navArgument("fromProducts") {
                        type = NavType.BoolType
                        defaultValue = false
                    },
                    navArgument("storeHint") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    },
                ),
            ) { entry ->
                val fromProducts = entry.arguments?.getBoolean("fromProducts") ?: false
                val rawHint = entry.arguments?.getString("storeHint")
                NearbyScreen(
                    initialSupermarketMode = fromProducts,
                    productStoreHint = rawHint?.takeIf { it.isNotBlank() },
                )
            }
            composable(Routes.RECIPES) {
                RecipesScreen(onOpenMealPlan = { navController.navigate(Routes.MEAL_PLAN) })
            }
            composable(Routes.MEAL_PLAN) {
                MealPlanScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.ACCOUNT) {
                AccountScreen(onBack = { navController.popBackStack() }, onSignOut = onSignOut)
            }
            composable(Routes.SCANNER) {
                ScannerScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}

