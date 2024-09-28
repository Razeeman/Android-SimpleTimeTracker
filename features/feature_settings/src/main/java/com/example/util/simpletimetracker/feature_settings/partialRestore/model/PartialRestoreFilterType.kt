package com.example.util.simpletimetracker.feature_settings.partialRestore.model

import android.os.Parcelable
import com.example.util.simpletimetracker.feature_base_adapter.recordFilter.FilterViewData
import kotlinx.parcelize.Parcelize

sealed interface PartialRestoreFilterType : FilterViewData.Type, Parcelable {
    @Parcelize
    object Activities : PartialRestoreFilterType

    @Parcelize
    object Categories : PartialRestoreFilterType

    @Parcelize
    object Tags : PartialRestoreFilterType

    @Parcelize
    object Records : PartialRestoreFilterType

    @Parcelize
    object ActivityFilters : PartialRestoreFilterType

    @Parcelize
    object FavouriteComments : PartialRestoreFilterType

    @Parcelize
    object FavouriteIcons : PartialRestoreFilterType

    @Parcelize
    object ComplexRules : PartialRestoreFilterType
}