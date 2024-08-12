package com.uycode.wit

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector

enum class Screens(val route: String, val title: String, val icon: ImageVector) {
    RECENT("recent", "Recent", Icons.Default.Call),
    SEARCH("search", "Search", Icons.Default.Search),
    CONTACT("contact", "Contact", Icons.Default.AccountBox)

}
