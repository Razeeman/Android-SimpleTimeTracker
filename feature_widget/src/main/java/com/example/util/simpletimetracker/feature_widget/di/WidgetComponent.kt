package com.example.util.simpletimetracker.feature_widget.di

import com.example.util.simpletimetracker.feature_widget.configure.view.WidgetConfigureActivity
import com.example.util.simpletimetracker.feature_widget.widget.WidgetProvider
import dagger.Subcomponent

@Subcomponent
interface WidgetComponent {

    fun inject(provider: WidgetProvider)

    fun inject(activity: WidgetConfigureActivity)
}