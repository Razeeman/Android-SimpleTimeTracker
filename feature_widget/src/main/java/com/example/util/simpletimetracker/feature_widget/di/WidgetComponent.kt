package com.example.util.simpletimetracker.feature_widget.di

import com.example.util.simpletimetracker.feature_widget.configure.view.WidgetConfigureActivity
import com.example.util.simpletimetracker.feature_widget.universal.WidgetUniversalProvider
import com.example.util.simpletimetracker.feature_widget.universal.activity.view.WidgetUniversalActivity
import com.example.util.simpletimetracker.feature_widget.universal.activity.view.WidgetUniversalFragment
import com.example.util.simpletimetracker.feature_widget.widget.WidgetProvider
import com.example.util.simpletimetracker.feature_widget.widget.WidgetTagSelectionActivity
import dagger.Subcomponent

@Subcomponent
interface WidgetComponent {

    fun inject(provider: WidgetProvider)

    fun inject(provider: WidgetUniversalProvider)

    fun inject(activity: WidgetConfigureActivity)

    fun inject(activity: WidgetTagSelectionActivity)

    fun inject(activity: WidgetUniversalActivity)

    fun inject(activity: WidgetUniversalFragment)
}