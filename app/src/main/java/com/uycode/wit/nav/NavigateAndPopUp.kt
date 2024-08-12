package com.uycode.wit.nav
import androidx.navigation.NavController

fun NavController.navigateAndPopUp(route: String) {
    navigate(route) {
        popUpTo(currentBackStackEntry?.destination?.route ?: return@navigate) {
            inclusive = true
        }
    }
}