package com.example.util.simpletimetracker.feature_statistics_detail.viewData

import androidx.annotation.ColorInt
import com.example.util.simpletimetracker.core.viewData.RecordTypeIcon

data class StatisticsDetailPreviewViewData(
    val name: String,
    val iconId: RecordTypeIcon? = null,
    @ColorInt val color: Int
)