package dk.au.emilieweiss.mylittlelibrary.ui.navigation

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object Library : Screen("library")
    data object Scan : Screen("scan")
    data object Friends : Screen("friends")
    data object FriendSearch : Screen("friend_search")
    data object Requests : Screen("requests")
    data object BookDetail : Screen("book_detail/{bookId}") {
        fun createRoute(bookId: String) = "book_detail/$bookId"
    }
}