package com.example.util.simpletimetracker.feature_settings_views

sealed interface SettingsTextColor {
    object Default : SettingsTextColor
    object Attention : SettingsTextColor
    object Success : SettingsTextColor
}