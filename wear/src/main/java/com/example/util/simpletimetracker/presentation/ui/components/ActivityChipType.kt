package com.example.util.simpletimetracker.presentation.ui.components

sealed interface ActivityChipType {
    object Base : ActivityChipType
    object Repeat : ActivityChipType
}