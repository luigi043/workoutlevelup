package com.example.workoutlevelup.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.workoutlevelup.ui.screens.BadgesScreen
import com.example.workoutlevelup.ui.screens.CalendarScreen
import com.example.workoutlevelup.ui.screens.DailyWorkoutScreen
import com.example.workoutlevelup.ui.theme.DeepNavy
import com.example.workoutlevelup.ui.theme.NeonBlue
import com.example.workoutlevelup.ui.theme.NeutralGray
import com.example.workoutlevelup.viewmodel.WorkoutViewModel
import java.time.LocalDate

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Calendar : Screen("calendar", "Calendar",   Icons.Default.CalendarMonth)
    object Workout  : Screen("workout/{date}", "Today", Icons.Default.FitnessCenter)
    object Badges   : Screen("badges",  "Badges",     Icons.Default.EmojiEvents)
}

private val bottomNavItems = listOf(Screen.Calendar, Screen.Workout, Screen.Badges)

@Composable
fun AppNavigation(viewModel: WorkoutViewModel = viewModel()) {
    val navController = rememberNavController()
    val today = LocalDate.now().toString()

    Scaffold(
        containerColor = DeepNavy,
        bottomBar = {
            NavigationBar(containerColor = DeepNavy) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                bottomNavItems.forEach { screen ->
                    val selected = currentDestination?.hierarchy?.any {
                        it.route == screen.route
                    } == true

                    NavigationBarItem(
                        icon  = { Icon(screen.icon, contentDescription = screen.label) },
                        label = { Text(screen.label) },
                        selected = selected,
                        onClick = {
                            val route = if (screen is Screen.Workout)
                                "workout/$today"
                            else screen.route

                            navController.navigate(route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = screen !is Screen.Workout
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor   = NeonBlue,
                            selectedTextColor   = NeonBlue,
                            unselectedIconColor = NeutralGray,
                            unselectedTextColor = NeutralGray,
                            indicatorColor      = NeonBlue.copy(alpha = 0.15f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController    = navController,
            startDestination = Screen.Calendar.route,
            modifier         = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Calendar.route) {
                CalendarScreen(
                    viewModel = viewModel,
                    onDayClick = { date ->
                        navController.navigate("workout/$date")
                    }
                )
            }

            composable(
                route = "workout/{date}",
                arguments = listOf(navArgument("date") { type = NavType.StringType })
            ) { backStackEntry ->
                val dateStr = backStackEntry.arguments?.getString("date") ?: today
                val date = runCatching { LocalDate.parse(dateStr) }.getOrDefault(LocalDate.now())
                DailyWorkoutScreen(
                    viewModel    = viewModel,
                    selectedDate = date,
                    onWorkoutComplete = {
                        if (navController.currentBackStackEntry == backStackEntry) {
                            navController.navigate(Screen.Badges.route) {
                                launchSingleTop = true
                            }
                        }
                    }
                )
            }

            composable(Screen.Badges.route) {
                BadgesScreen(viewModel = viewModel)
            }
        }
    }
}
