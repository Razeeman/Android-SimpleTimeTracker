package com.example.util.simpletimetracker.navigation.params.screen

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed class ArchiveDialogParams : Parcelable, ScreenParams {

    @Parcelize
    data class Activity(val id: Long) : ArchiveDialogParams()

    @Parcelize
    data class RecordTag(val id: Long) : ArchiveDialogParams()
}