package com.example.util.simpletimetracker.feature_widget.interactor

import com.example.util.simpletimetracker.core.interactor.WidgetInteractor
import com.example.util.simpletimetracker.domain.model.WidgetType
import javax.inject.Inject

class WidgetInteractorImpl @Inject constructor(
    private val widgetManager: WidgetManager
) : WidgetInteractor {

    override fun updateWidget(widgetId: Int) = widgetManager.updateWidget(widgetId)

    override fun updateWidgets(types: List<WidgetType>) = widgetManager.updateWidgets(types)
}