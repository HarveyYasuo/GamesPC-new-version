package com.harvey.gamespc.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.harvey.gamespc.SharedViewModel
import com.harvey.gamespc.ui.TopBarNavItem
import com.harvey.gamespc.ui.screens.ChatScreen
import com.harvey.gamespc.ui.screens.DetailScreen
import com.harvey.gamespc.ui.screens.DetailViewModel
import com.harvey.gamespc.ui.screens.HomeScreen

@Composable
fun NavigationGraph(
    navController: NavHostController,
    sharedViewModel: SharedViewModel,
    searchQuery: String,
    onItemClick: (itemId: String, categoryName: String) -> Unit
) {
    NavHost(navController, startDestination = TopBarNavItem.Home.route) {
        composable(TopBarNavItem.Home.route) {
            HomeScreen(
                sharedViewModel = sharedViewModel,
                searchQuery = searchQuery,
                onItemClick = onItemClick
            )
        }
        composable(TopBarNavItem.Chat.route) {
            ChatScreen()
        }
        composable("search") {
            // Placeholder for Search Screen
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Search Screen")
            }
        }
        composable(
            route = "detail/{itemId}/{categoryName}",
            arguments = listOf(
                navArgument("itemId") { type = NavType.StringType },
                navArgument("categoryName") { type = NavType.StringType }
            )
        ) {
            val itemId = it.arguments?.getString("itemId")
            val categoryName = it.arguments?.getString("categoryName")

            if (itemId != null && categoryName != null) {
                val detailViewModelFactory = DetailViewModel.Factory(itemId, categoryName, sharedViewModel)
                val detailViewModel: DetailViewModel = viewModel(factory = detailViewModelFactory)
                DetailScreen(detailViewModel = detailViewModel)
            }
        }
    }
}

