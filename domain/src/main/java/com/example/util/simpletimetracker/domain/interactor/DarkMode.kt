package com.example.util.simpletimetracker.domain.interactor

sealed interface DarkMode {
    object System : DarkMode
    object Enabled : DarkMode
    object Disabled : DarkMode
}