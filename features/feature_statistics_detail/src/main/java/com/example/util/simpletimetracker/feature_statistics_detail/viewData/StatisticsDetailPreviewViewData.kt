package com.example.util.simpletimetracker.feature_statistics_detail.viewData

import androidx.annotation.ColorInt
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_views.viewData.RecordTypeIcon

data class StatisticsDetailPreviewViewData(
    val id: Long,
    val name: String,
    val iconId: RecordTypeIcon? = null,
    @ColorInt val color: Int
) : ViewHolderType {

    override fun getUniqueId(): Long = id

    override fun isValidType(other: ViewHolderType): Boolean =
        other is StatisticsDetailPreviewViewData
}