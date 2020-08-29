package com.example.util.simpletimetracker.feature_change_record_type.extra

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ChangeRecordTypeExtra(
    val id: Long,
    val width: Int?,
    val height: Int?,
    val asRow: Boolean
) : Parcelable