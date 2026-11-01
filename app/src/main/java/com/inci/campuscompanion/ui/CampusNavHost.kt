package com.inci.campuscompanion.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.inci.campuscompanion.TaskScreen
import com.inci.campuscompanion.model.Task

object CampusRoutes {
    const val TASKS = "tasks"
    const val EVENTS = "events"
}

@Composable
fun CampusNavHost(
    isDarkMode: Boolean,
    tasks: List<Task>,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    // The NavController lives here; screens only receive callbacks.
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = CampusRoutes.TASKS,
        modifier = modifier
    ) {
        composable(CampusRoutes.TASKS) {
            TaskScreen(
                isDarkMode = isDarkMode,
                onToggleDarkMode = { viewModel.toggleDarkMode(it) },
                tasks = tasks,
                onToggleTask = { viewModel.toggleTaskCompletion(it) },
                onAddTask = { title, description -> viewModel.upsertTask(title, description) },
                onOpenEvents = { navController.navigate(CampusRoutes.EVENTS) }
            )
        }
        composable(CampusRoutes.EVENTS) {
            EventsScreen()
        }
    }
}
