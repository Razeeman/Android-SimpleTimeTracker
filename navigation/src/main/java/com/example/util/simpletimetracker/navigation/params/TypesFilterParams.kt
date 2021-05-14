package com.example.util.simpletimetracker.navigation.params

import android.os.Parcelable
import com.example.util.simpletimetracker.domain.model.ChartFilterType
import kotlinx.android.parcel.Parcelize

@Parcelize
data class TypesFilterParams(
    val filterType: ChartFilterType = ChartFilterType.ACTIVITY,
    // activity tag or activity depending on filter type
    val selectedIds: List<Long> = emptyList(),
    val filteredRecordTags: List<Long> = emptyList()
) : Parcelable