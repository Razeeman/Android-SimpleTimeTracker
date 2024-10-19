package com.example.util.simpletimetracker.domain.interactor

import com.example.util.simpletimetracker.domain.model.WidgetType

interface WidgetInteractor {

    fun updateSingleWidget(widgetId: Int)

    fun updateSingleWidgets(typeIds: List<Long>)

    fun updateStatisticsWidget(widgetId: Int)

    fun updateQuickSettingsWidget(widgetId: Int)

    fun updateWidgets(type: WidgetType)
}