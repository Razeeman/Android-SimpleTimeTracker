package com.example.util.simpletimetracker.feature_color_selection.api

import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType

data class GradientControlsViewData(
    val endpoint: GradientEndpoint,
    val angleDegrees: Int,
    val startColor: Int,
    val endColor: Int,
) : ViewHolderType {

    override fun getUniqueId(): Long = 1L

    override fun isValidType(other: ViewHolderType): Boolean = other is GradientControlsViewData
}
