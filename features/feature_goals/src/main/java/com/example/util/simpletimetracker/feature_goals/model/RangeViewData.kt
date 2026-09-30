package com.example.util.simpletimetracker.feature_goals.model

import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType

data class RangeViewData(
    val items: List<ViewHolderType>,
    val hasHiddenFinishedGoals: Boolean,
)