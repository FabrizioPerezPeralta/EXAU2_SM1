package com.example.perezperaltafabrizio_exau2_sm1.ui.navigation

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.perezperaltafabrizio_exau2_sm1.ui.screens.CatalogScreen
import com.example.perezperaltafabrizio_exau2_sm1.ui.screens.ProductDetailScreen
import com.example.perezperaltafabrizio_exau2_sm1.viewmodel.ProductDetailViewModel
import com.example.perezperaltafabrizio_exau2_sm1.viewmodel.ProductViewModel

@Composable
fun AppNavigation(application: Application) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "catalog"
    ) {
        composable(route = "catalog") {
            val factory = ProductViewModel.Factory(application)
            val viewModel: ProductViewModel = viewModel(factory = factory)
            CatalogScreen(
                viewModel = viewModel,
                onProductClick = { productId ->
                    navController.navigate("detail/$productId")
                }
            )
        }

        composable(
            route = "detail/{productId}",
            arguments = listOf(navArgument("productId") { type = NavType.IntType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getInt("productId") ?: 0
            val factory = ProductDetailViewModel.Factory(productId, application)
            val viewModel: ProductDetailViewModel = viewModel(factory = factory)
            ProductDetailScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
