package com.example.util.simpletimetracker.navigation.params

import android.os.Parcelable
import com.example.util.simpletimetracker.domain.model.ChartFilterType
import kotlinx.android.parcel.Parcelize

@Parcelize
data class TypesFilterParams(
    val filterType: ChartFilterType = ChartFilterType.ACTIVITY,
    // activity tag or activity depending on filter type
    val selectedIds: List<Long> = emptyList(),
    val filteredRecordTags: List<FilteredRecordTag> = emptyList()
) : Parcelable {

    sealed class FilteredRecordTag : Parcelable {

        @Parcelize
        data class Tagged(val id: Long) : FilteredRecordTag()

        @Parcelize
        data class Untagged(val typeId: Long) : FilteredRecordTag()
    }
}