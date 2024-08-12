package com.uycode.wit.nav

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.uycode.wit.Screens
import com.uycode.wit.screens.ContactScreen
import com.uycode.wit.screens.RecentScreen
import com.uycode.wit.screens.SearchScreen

@Composable
fun CustomNavHost(
    modifier: Modifier = Modifier,
    startDestination: String = Screens.RECENT.route,
    navController: NavHostController
) {

    NavHost(navController, startDestination = startDestination, modifier = modifier) {
        composable(route = Screens.RECENT.route) {
            RecentScreen()
        }
        composable(route = Screens.SEARCH.route) {
            SearchScreen()
        }
        composable(route = Screens.CONTACT.route) {
            ContactScreen()
        }
    }

}