package com.example.todobase.nav

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class NavItem(val route: String, val title: String, val icon: ImageVector) {
    object Home : NavItem("home", "Home", Icons.Default.Home)
    object Map : NavItem("map", "Map", Icons.Default.LocationOn) {
        const val routeWithArgs = "map?lat={lat}&lng={lng}"
        fun createRoute(lat: Double, lng: Double) = "map?lat=$lat&lng=$lng"
    }
    object Profile : NavItem("profile", "Profile", Icons.Default.Person)
}