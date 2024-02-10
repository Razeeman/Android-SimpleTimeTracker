package com.example.util.simpletimetracker.domain.model

sealed interface IconImageState {
    object Chooser : IconImageState
    object Search : IconImageState
}