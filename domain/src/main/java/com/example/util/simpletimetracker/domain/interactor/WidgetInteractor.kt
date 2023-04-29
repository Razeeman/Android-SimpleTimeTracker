package com.example.util.simpletimetracker.domain.interactor

import com.example.util.simpletimetracker.domain.model.WidgetType

interface WidgetInteractor {

    fun updateWidget(widgetId: Int)

    fun updateStatisticsWidget(widgetId: Int)

    fun updateQuickSettingsWidget(widgetId: Int)

    fun updateWidgets(types: List<WidgetType> = emptyList())
}