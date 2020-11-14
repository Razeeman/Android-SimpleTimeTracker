package com.example.util.simpletimetracker.navigation.params

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ChangeCategoryParams(
    // id of the Category to change, if no id - creating new one.
    val id: Long = 0
    // TODO send size also
) : Parcelable