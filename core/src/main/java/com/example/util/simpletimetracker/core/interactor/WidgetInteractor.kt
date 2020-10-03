package com.example.util.simpletimetracker.core.interactor

import com.example.util.simpletimetracker.core.manager.WidgetManager
import javax.inject.Inject

class WidgetInteractor @Inject constructor(
    private val widgetManager: WidgetManager
) {

    fun updateWidget(widgetId: Int) = widgetManager.updateWidget(widgetId)

    fun updateWidgets() = widgetManager.updateWidgets()
}