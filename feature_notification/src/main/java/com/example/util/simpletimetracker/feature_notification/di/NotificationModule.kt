package com.example.util.simpletimetracker.feature_notification.di

import com.example.util.simpletimetracker.core.manager.NotificationManager
import com.example.util.simpletimetracker.feature_notification.NotificationManagerImpl
import dagger.Binds
import dagger.Module
import javax.inject.Singleton

@Module
abstract class NotificationModule {

    @Binds
    @Singleton
    abstract fun getNotificationManager(impl: NotificationManagerImpl): NotificationManager
}