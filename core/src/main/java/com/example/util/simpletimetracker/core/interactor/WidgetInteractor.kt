package com.example.util.simpletimetracker.core.interactor

import com.example.util.simpletimetracker.domain.model.WidgetType

interface WidgetInteractor {

    fun updateWidget(widgetId: Int)

    fun updateStatisticsWidget(widgetId: Int)

    fun updateWidgets(types: List<WidgetType> = emptyList())
}