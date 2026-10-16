package com.windingmaster.presentation.navigation

import androidx.annotation.StringRes
import com.windingmaster.R

sealed class Screen(val base: String, val route: String, @StringRes val titleRes: Int) {

    object Motors : Screen("motors", "motors", R.string.nav_motors)

    object MotorEdit : Screen("motor_edit", "motor_edit?motorId={motorId}", R.string.motor_edit_title) {
        fun create(motorId: Long = -1L) = "motor_edit?motorId=$motorId"
    }

    object Calculator : Screen("calculator", "calculator", R.string.nav_calculator)

    object Checklist : Screen("checklist", "checklist?motorId={motorId}", R.string.nav_checklist) {
        fun create(motorId: Long = -1L) = "checklist?motorId=$motorId"
    }

    object Profile : Screen("profile", "profile?motorId={motorId}", R.string.nav_profile) {
        fun create(motorId: Long = -1L) = "profile?motorId=$motorId"
    }

    object Notes : Screen("notes", "notes", R.string.nav_notes)

    object NoteEdit : Screen(
        "note_edit", "note_edit?noteId={noteId}&motorId={motorId}", R.string.note_edit_title
    ) {
        fun create(noteId: Long = -1L, motorId: Long = -1L) =
            "note_edit?noteId=$noteId&motorId=$motorId"
    }

    object Settings : Screen("settings", "settings", R.string.nav_settings)
}

val bottomNavScreens: List<Screen> = listOf(
    Screen.Motors, Screen.Calculator, Screen.Checklist, Screen.Profile, Screen.Notes
)

val allScreens: List<Screen> = bottomNavScreens + listOf(
    Screen.MotorEdit, Screen.NoteEdit, Screen.Settings
)
