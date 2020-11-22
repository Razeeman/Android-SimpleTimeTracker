package com.example.util.simpletimetracker.feature_dialogs.chartFilter.viewData

import com.example.util.simpletimetracker.core.view.buttonsRowView.ButtonsRowViewData
import com.example.util.simpletimetracker.feature_dialogs.chartFilter.model.ChartFilterType

data class ChartFilterTypeViewData(
    val filterType: ChartFilterType,
    override val name: String,
    override val isSelected: Boolean
) : ButtonsRowViewData() {

    override val id: Long = filterType.ordinal.toLong()
}