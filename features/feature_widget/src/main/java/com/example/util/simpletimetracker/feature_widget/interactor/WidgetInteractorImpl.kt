package com.example.util.simpletimetracker.feature_widget.interactor

import com.example.util.simpletimetracker.domain.interactor.WidgetInteractor
import com.example.util.simpletimetracker.domain.model.WidgetType
import javax.inject.Inject

class WidgetInteractorImpl @Inject constructor(
    private val widgetManager: WidgetManager,
    private val widgetViewsHolder: WidgetViewsHolder,
) : WidgetInteractor {

    override fun initializeCachedViews() = widgetViewsHolder.initialize()

    override fun updateSingleWidget(widgetId: Int) = widgetManager.updateSingleWidget(widgetId)

    override fun updateSingleWidgets(typeIds: List<Long>) = widgetManager.updateSingleWidgets(typeIds)

    override fun updateStatisticsWidget(widgetId: Int) = widgetManager.updateStatisticsWidget(widgetId)

    override fun updateQuickSettingsWidget(widgetId: Int) = widgetManager.updateQuickSettingsWidget(widgetId)

    override fun updateWidgets(type: WidgetType) = widgetManager.updateWidgets(listOf(type))
}