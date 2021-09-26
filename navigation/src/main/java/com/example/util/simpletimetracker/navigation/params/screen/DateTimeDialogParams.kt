package com.example.util.simpletimetracker.navigation.params.screen

import android.os.Parcelable
import com.example.util.simpletimetracker.domain.model.DayOfWeek
import kotlinx.parcelize.Parcelize

@Parcelize
data class DateTimeDialogParams(
    val tag: String? = null,
    val useMilitaryTime: Boolean = false,
    val type: DateTimeDialogType = DateTimeDialogType.DATETIME(),
    val timestamp: Long = 0,
    val firstDayOfWeek: DayOfWeek = DayOfWeek.MONDAY,
) : Parcelable, ScreenParams