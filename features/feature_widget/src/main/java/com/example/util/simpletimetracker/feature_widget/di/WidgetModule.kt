package com.example.util.simpletimetracker.feature_widget.di

import com.example.util.simpletimetracker.domain.interactor.WidgetInteractor
import com.example.util.simpletimetracker.feature_widget.interactor.WidgetInteractorImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface WidgetModule {

    @Binds
    fun WidgetInteractorImpl.bindWidgetInteractor(): WidgetInteractor
}