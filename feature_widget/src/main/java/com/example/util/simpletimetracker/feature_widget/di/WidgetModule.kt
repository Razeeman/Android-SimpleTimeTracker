package com.example.util.simpletimetracker.feature_widget.di

import com.example.util.simpletimetracker.core.interactor.WidgetInteractor
import com.example.util.simpletimetracker.feature_widget.interactor.WidgetInteractorImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class WidgetModule {

    @Binds
    abstract fun getWidgetInteractor(impl: WidgetInteractorImpl): WidgetInteractor
}