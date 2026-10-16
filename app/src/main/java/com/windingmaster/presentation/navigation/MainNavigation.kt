package com.windingmaster.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.windingmaster.R
import com.windingmaster.presentation.calculator.CalculatorScreen
import com.windingmaster.presentation.checklist.ChecklistScreen
import com.windingmaster.presentation.notes.NoteEditScreen
import com.windingmaster.presentation.notes.NotesScreen
import com.windingmaster.presentation.profile.MotorEditScreen
import com.windingmaster.presentation.profile.MotorsScreen
import com.windingmaster.presentation.profile.ProfileScreen
import com.windingmaster.presentation.settings.SettingsScreen

private fun Screen.icon(): ImageVector = when (this) {
    Screen.Motors -> Icons.Default.Build
    Screen.Calculator -> Icons.Default.Calculate
    Screen.Checklist -> Icons.Default.Checklist
    Screen.Profile -> Icons.Default.Person
    Screen.Notes -> Icons.Default.Description
    else -> Icons.Default.Settings
}

private fun longArg(name: String) = navArgument(name) {
    type = NavType.LongType
    defaultValue = -1L
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainNavigation() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentBase = backStackEntry?.destination?.route?.substringBefore('?')
    val currentScreen = allScreens.firstOrNull { it.base == currentBase }
    val isTopLevel = bottomNavScreens.any { it.base == currentBase }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(currentScreen?.titleRes ?: R.string.app_name)) },
                navigationIcon = {
                    if (currentScreen != null && !isTopLevel) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.action_back))
                        }
                    }
                },
                actions = {
                    if (currentBase != Screen.Settings.base) {
                        IconButton(onClick = {
                            navController.navigate(Screen.Settings.route) { launchSingleTop = true }
                        }) {
                            Icon(Icons.Default.Settings, stringResource(R.string.nav_settings))
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (isTopLevel) {
                NavigationBar {
                    bottomNavScreens.forEach { screen ->
                        NavigationBarItem(
                            selected = currentBase == screen.base,
                            onClick = {
                                navController.navigate(screen.base) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(screen.icon(), contentDescription = null) },
                            label = {
                                Text(
                                    stringResource(screen.titleRes),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Calculator.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Motors.route) {
                MotorsScreen(
                    onAdd = { navController.navigate(Screen.MotorEdit.create()) },
                    onEdit = { navController.navigate(Screen.MotorEdit.create(it)) }
                )
            }
            composable(Screen.MotorEdit.route, arguments = listOf(longArg("motorId"))) {
                MotorEditScreen(onDone = { navController.popBackStack() })
            }
            composable(Screen.Calculator.route) { CalculatorScreen() }
            composable(Screen.Checklist.route, arguments = listOf(longArg("motorId"))) {
                ChecklistScreen()
            }
            composable(Screen.Profile.route, arguments = listOf(longArg("motorId"))) {
                ProfileScreen(
                    onSelectMotor = { navController.navigate(Screen.Profile.create(it)) },
                    onEditMotor = { navController.navigate(Screen.MotorEdit.create(it)) },
                    onOpenChecklist = { navController.navigate(Screen.Checklist.create(it)) },
                    onNewNote = { navController.navigate(Screen.NoteEdit.create(motorId = it)) },
                    onOpenNote = { noteId, motorId ->
                        navController.navigate(Screen.NoteEdit.create(noteId, motorId))
                    }
                )
            }
            composable(Screen.Notes.route) {
                NotesScreen(
                    onAdd = { navController.navigate(Screen.NoteEdit.create()) },
                    onEdit = { navController.navigate(Screen.NoteEdit.create(noteId = it)) }
                )
            }
            composable(
                Screen.NoteEdit.route,
                arguments = listOf(longArg("noteId"), longArg("motorId"))
            ) {
                NoteEditScreen(onDone = { navController.popBackStack() })
            }
            composable(Screen.Settings.route) { SettingsScreen() }
        }
    }
}
