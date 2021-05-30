package com.example.util.simpletimetracker.navigation.params

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

sealed class ArchiveDialogParams : Parcelable {

    @Parcelize
    data class Activity(val id: Long) : ArchiveDialogParams()

    @Parcelize
    data class RecordTag(val id: Long) : ArchiveDialogParams()
}