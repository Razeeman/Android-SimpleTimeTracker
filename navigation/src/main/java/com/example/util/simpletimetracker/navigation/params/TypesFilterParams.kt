package com.example.util.simpletimetracker.navigation.params

import android.os.Parcelable
import com.example.util.simpletimetracker.domain.model.ChartFilterType
import kotlinx.android.parcel.Parcelize

@Parcelize
data class TypesFilterParams(
    val selectedIds: List<Long> = emptyList(),
    val filterType: ChartFilterType = ChartFilterType.ACTIVITY
) : Parcelable