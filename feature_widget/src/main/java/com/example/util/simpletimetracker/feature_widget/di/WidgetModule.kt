package com.example.util.simpletimetracker.feature_widget.di

import com.example.util.simpletimetracker.domain.manager.WidgetManager
import com.example.util.simpletimetracker.feature_widget.widget.WidgetManagerImpl
import dagger.Binds
import dagger.Module
import javax.inject.Singleton

@Module
abstract class WidgetModule {

    @Binds
    @Singleton
    abstract fun getWidgetManager(impl: WidgetManagerImpl): WidgetManager
}