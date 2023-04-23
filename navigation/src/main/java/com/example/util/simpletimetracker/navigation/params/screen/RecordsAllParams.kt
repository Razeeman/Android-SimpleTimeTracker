package com.example.util.simpletimetracker.navigation.params.screen

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class RecordsAllParams(
    val filter: List<RecordsFilterParam> = emptyList(),
) : Parcelable, ScreenParams