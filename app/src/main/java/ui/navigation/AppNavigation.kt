package dk.au.emilieweiss.mylittlelibrary.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dk.au.emilieweiss.mylittlelibrary.ui.auth.LoginScreen
import dk.au.emilieweiss.mylittlelibrary.ui.auth.RegisterScreen
import dk.au.emilieweiss.mylittlelibrary.ui.friends.FriendSearchScreen
import dk.au.emilieweiss.mylittlelibrary.ui.friends.FriendsScreen
import dk.au.emilieweiss.mylittlelibrary.ui.library.BookDetailScreen
import dk.au.emilieweiss.mylittlelibrary.ui.library.LibraryScreen
import dk.au.emilieweiss.mylittlelibrary.ui.requests.RequestsScreen
import dk.au.emilieweiss.mylittlelibrary.ui.scan.ScanScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute in bottomNavItems.map { it.screen.route }

    Scaffold(
        bottomBar = { if (showBottomBar) BottomBar(navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Login.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Login.route) {
                LoginScreen(
                    onLoginClick = {
                        navController.navigate(Screen.Library.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onRegisterClick = { navController.navigate(Screen.Register.route) }
                )
            }
            composable(Screen.Register.route) {
                RegisterScreen(onBackClick = { navController.popBackStack() })
            }
            composable(Screen.Library.route) {
                LibraryScreen(
                    onBookClick = { id -> navController.navigate(Screen.BookDetail.createRoute(id)) }
                )
            }
            composable(
                route = Screen.BookDetail.route,
                arguments = listOf(navArgument("bookId") { type = NavType.StringType })
            ) { entry ->
                BookDetailScreen(
                    bookId = entry.arguments?.getString("bookId") ?: "",
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable(Screen.Scan.route) { ScanScreen() }
            composable(Screen.Friends.route) {
                FriendsScreen(onSearchClick = { navController.navigate(Screen.FriendSearch.route) })
            }
            composable(Screen.FriendSearch.route) {
                FriendSearchScreen(onBackClick = { navController.popBackStack() })
            }
            composable(Screen.Requests.route) { RequestsScreen() }
        }
    }
}