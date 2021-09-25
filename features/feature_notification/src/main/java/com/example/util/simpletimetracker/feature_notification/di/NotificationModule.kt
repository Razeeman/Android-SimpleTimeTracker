package com.example.util.simpletimetracker.feature_notification.di

import com.example.util.simpletimetracker.core.interactor.NotificationGoalTimeInteractor
import com.example.util.simpletimetracker.core.interactor.NotificationInactivityInteractor
import com.example.util.simpletimetracker.core.interactor.NotificationTypeInteractor
import com.example.util.simpletimetracker.feature_notification.goalTime.interactor.NotificationGoalTimeInteractorImpl
import com.example.util.simpletimetracker.feature_notification.inactivity.interactor.NotificationInactivityInteractorImpl
import com.example.util.simpletimetracker.feature_notification.recordType.interactor.NotificationTypeInteractorImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class NotificationModule {

    @Binds
    abstract fun getTypeInteractor(impl: NotificationTypeInteractorImpl): NotificationTypeInteractor

    @Binds
    abstract fun getInactivityInteractor(impl: NotificationInactivityInteractorImpl): NotificationInactivityInteractor

    @Binds
    abstract fun getGoalTimeInteractor(impl: NotificationGoalTimeInteractorImpl): NotificationGoalTimeInteractor
}