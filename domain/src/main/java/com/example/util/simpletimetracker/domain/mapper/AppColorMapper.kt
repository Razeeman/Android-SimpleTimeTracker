package com.example.util.simpletimetracker.domain.mapper

import com.example.util.simpletimetracker.domain.model.AppColor

interface AppColorMapper {

    fun mapToColorInt(color: AppColor, isDarkTheme: Boolean): Int
}