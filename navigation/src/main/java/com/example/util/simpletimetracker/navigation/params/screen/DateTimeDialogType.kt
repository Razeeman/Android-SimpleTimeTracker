package com.example.util.simpletimetracker.navigation.params.screen

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed class DateTimeDialogType : Parcelable {

    @Parcelize
    object DATE : DateTimeDialogType()

    @Parcelize
    data class DATETIME(val initialTab: Tab = Tab.TIME) : DateTimeDialogType()

    enum class Tab {
        DATE, TIME
    }
}