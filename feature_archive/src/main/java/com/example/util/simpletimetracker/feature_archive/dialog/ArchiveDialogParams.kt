package com.example.util.simpletimetracker.feature_archive.dialog

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

sealed class ArchiveDialogParams : Parcelable {

    @Parcelize
    data class Activity(val id: Long) : ArchiveDialogParams()

    @Parcelize
    data class RecordTag(val id: Long) : ArchiveDialogParams()
}