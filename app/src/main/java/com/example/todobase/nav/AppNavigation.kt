package com.example.todobase.nav

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.todobase.data.repo.AuthRepo
import com.example.todobase.nav.ui.theme.TodoBaseTheme
import com.example.todobase.ui.HomeScreen
import com.example.todobase.ui.LoginScreen
import com.example.todobase.ui.MapScreen
import com.example.todobase.ui.ProfileScreen
import com.example.todobase.ui.RegisterScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    var expanded by remember { mutableStateOf(false) }
    val authRepo = remember { AuthRepo() }
    val bottomNavItem = listOf(
        NavItem.Home,
        NavItem.Map,
        NavItem.Profile
    )
    
    val navBckStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBckStackEntry?.destination?.route
    
    val showBottombar = bottomNavItem.any { item -> currentRoute?.startsWith(item.route) == true }
    
    val startDestination = if (authRepo.isUserLoggedIn) NavItem.Home.route else "login"
    
    Scaffold (
        topBar = {
            if(authRepo.isUserLoggedIn){
                TopAppBar(
                    title = {
                        Text("Favorite Place") 
                    },
                    actions = {
                        Box() {
                            IconButton(
                                onClick = { 
                                    expanded = true 
                                }
                            ) {
                                Text(":")    
                            }

                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { 
                                        Text("Logout") 
                                    },
                                    onClick = {
                                        authRepo.logout()
                                        navController.navigate("login"){
                                            popUpTo("home"){
                                                inclusive = true
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                )
            }
        },
        bottomBar = {
            if(showBottombar){
                NavigationBar { 
                    bottomNavItem.forEach { i->
                        NavigationBarItem(
                            selected = currentRoute?.startsWith(i.route) == true,
                            onClick = {
                                if (i.route == NavItem.Home.route) {
                                    navController.popBackStack(NavItem.Home.route, inclusive = false)
                                } else {
                                    navController.navigate(i.route) {
                                        popUpTo(NavItem.Home.route) {
                                            saveState = false
                                        }
                                        launchSingleTop = true
                                    }
                                }
                            },
                            label = { Text(i.title) },
                            icon = {
                                Icon(i.icon, "Icon Nav")
                            }
                        )
                    }
                }
            }
        }
    ) { p ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(p)
        ) {
            NavHost(navController, startDestination) {
                composable("login") {
                    LoginScreen(
                        onLoginSuccess = {
                            navController.navigate("home") {
                                popUpTo("login") {
                                    inclusive = true
                                }
                            }
                        },
                        onGoToRegister = {
                            navController.navigate("register")
                        }
                    )
                }

                composable("register") {
                    RegisterScreen(
                        onRegisterSuccess = {
                            navController.navigate("login")
                        },
                        onGoToLogin = {
                            navController.navigate("login")
                        }
                    )
                }

                composable(NavItem.Home.route) {
                    HomeScreen(
                        onPlaceClick = { p ->
                            navController.navigate(
                                "map?lat=${p.latitude}&lng=${p.longitude}"
                            )
                        }
                    )
                }

                composable(
                    route = "map?lat={lat}&lng={lng}",
                    arguments = listOf(
                        navArgument("lat") {
                            type = NavType.StringType
                            nullable = true
                            defaultValue = null
                        },
                        navArgument("lng") {
                            type = NavType.StringType
                            nullable = true
                            defaultValue = null
                        }
                    )
                ) { bse ->
                    val lat = bse.arguments?.getString("lat")?.toDoubleOrNull()
                    val lng = bse.arguments?.getString("lng")?.toDoubleOrNull()
                    MapScreen(targetLat = lat, targetLng = lng)
                }

                composable(NavItem.Profile.route) {
                    ProfileScreen()
                }
            }
        }
    }
}
