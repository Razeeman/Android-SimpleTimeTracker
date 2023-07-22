package com.example.util.simpletimetracker.core.model

sealed interface NavigationTab {
    object RunningRecords : NavigationTab
    object Records : NavigationTab
    object Statistics : NavigationTab
    object Settings : NavigationTab
    object Goals : NavigationTab
}