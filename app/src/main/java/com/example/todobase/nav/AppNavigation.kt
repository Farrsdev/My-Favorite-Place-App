package com.example.todobase.nav

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
import com.example.todobase.ui.CardScreen
import com.example.todobase.ui.HomeScreen
import com.example.todobase.ui.LoginScreen
import com.example.todobase.ui.MapScreen
import com.example.todobase.ui.ProfileScreen
import com.example.todobase.ui.RegisterScreen
import com.example.todobase.ui.hapusGarisJarak
import org.maplibre.android.maps.MapLibreMap

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }
    var activeMapInstance by remember { mutableStateOf<MapLibreMap?>(null) }
    val authRepo = remember { AuthRepo() }
    val bottomNavItem = listOf(
        NavItem.Home,
        NavItem.Map,
        NavItem.Profile,
        NavItem.Card
    )
    
    val navBckStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBckStackEntry?.destination?.route
    
    val showBottombar = bottomNavItem.any { item -> currentRoute?.startsWith(item.route) == true }
    
    val startDestination = if (authRepo.isUserLoggedIn) NavItem.Home.route else "login"
    var username by remember { mutableStateOf("") }

    LaunchedEffect(currentRoute, authRepo.isUserLoggedIn) {
        if (authRepo.isUserLoggedIn) {
            authRepo.getUserProfile { user, _ ->
                if (user != null) {
                    username = user.username
                } else if (username.isEmpty()) {
                    username = "Gagal Memuat Profil"
                }
            }
        } else {
            username = ""
        }
    }
    Scaffold (
        topBar = {
            if(authRepo.isUserLoggedIn){
                TopAppBar(
                    title = {
                        Text("Welcome, $username")
                    },
                    actions = {
                        Box() {
                            IconButton(
                                onClick = { 
                                    expanded = true 
                                }
                            ) {
                                Icon(Icons.Default.MoreVert, contentDescription = "Menu Opsi")
                            }

                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                if (currentRoute?.startsWith("map") == true) {
                                    DropdownMenuItem(
                                        text = { 
                                            Text("Hapus Garis") 
                                        },
                                        onClick = {
                                            expanded = false
                                            if (activeMapInstance != null) {
                                                hapusGarisJarak(activeMapInstance)
                                                Toast.makeText(context, "Garis pengukuran berhasil dihapus", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(context, "Peta belum siap", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    )
                                }

                                DropdownMenuItem(
                                    text = { 
                                        Text("Logout") 
                                    },
                                    onClick = {
                                        expanded = false
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
                            navController.navigate("login") {
                                popUpTo("login") {
                                    inclusive = true
                                }
                            }
                        },
                        onGoToLogin = {
                            navController.navigate("login") {
                                popUpTo("login") {
                                    inclusive = true
                                }
                            }
                        }
                    )
                }

                composable(NavItem.Home.route) {
                    HomeScreen(
                        onPlaceClick = { p ->
                            navController.navigate(
                                NavItem.Map.createRoute(p.latitude, p.longitude)
                            )
                        }
                    )
                }

                composable(
                    route = NavItem.Map.routeWithArgs,
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
                    MapScreen(
                        targetLat = lat,
                        targetLng = lng,
                        onMapReady = { map ->
                            activeMapInstance = map
                        }
                    )
                }

                composable(NavItem.Profile.route) {
                    ProfileScreen()
                }

                composable(NavItem.Card.route){
                    CardScreen()
                }
            }
        }
    }
}
