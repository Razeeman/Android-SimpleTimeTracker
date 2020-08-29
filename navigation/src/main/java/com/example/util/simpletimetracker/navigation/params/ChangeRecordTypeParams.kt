package com.example.util.simpletimetracker.navigation.params

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ChangeRecordTypeParams(
    // id of the RecordType to change, if no id - creating new one.
    val id: Long = 0,
    val width: Int? = null,
    val height: Int? = null,
    val asRow: Boolean = false
) : Parcelable