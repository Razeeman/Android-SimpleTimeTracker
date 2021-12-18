package com.example.util.simpletimetracker.domain.model

sealed class AppColor {

    data class Id(val colorId: Int) : AppColor()
    data class Hex(val colorHex: String) : AppColor()
}