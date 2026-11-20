package dk.au.emilieweiss.mylittlelibrary.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dk.au.emilieweiss.mylittlelibrary.ui.auth.AuthViewModel
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
    val authViewModel: AuthViewModel = viewModel()
    val authState by authViewModel.uiState.collectAsState()
    val startDestination = remember {
        if (authViewModel.uiState.value.isLoggedIn) Screen.Library.route else Screen.Login.route
    }
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute in bottomNavItems.map { it.screen.route }

    Scaffold(
        bottomBar = { if (showBottomBar) BottomBar(navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Login.route) {
                LoginScreen(
                    uiState = authState,
                    onSignIn = authViewModel::signIn,
                    onClearError = authViewModel::clearError,
                    onLoggedIn = {
                        navController.navigate(Screen.Library.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onRegisterClick = { navController.navigate(Screen.Register.route) }
                )
            }
            composable(Screen.Register.route) {
                RegisterScreen(
                    uiState = authState,
                    onSignUp = authViewModel::signUp,
                    onClearError = authViewModel::clearError,
                    onLoggedIn = {
                        navController.navigate(Screen.Library.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable(Screen.Library.route) {
                LibraryScreen(
                    onBookClick = { id -> navController.navigate(Screen.BookDetail.createRoute(id)) },
                    onSignOutClick = {
                        authViewModel.signOut()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(navController.graph.id) { inclusive = true }
                        }
                    }
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