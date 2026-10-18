package com.example.util.simpletimetracker.domain.color.model

data class AppColor(
    val colorId: Int,
    val colorInt: String,
    val gradient: Gradient?,
) {

    data class Gradient(
        val endColorId: Int,
        val endColorInt: String,
        val angleDegrees: Int,
    )
}