package com.example.util.simpletimetracker.di

import com.example.util.simpletimetracker.data_local.di.DataLocalModule
import com.example.util.simpletimetracker.data_local.di.DataLocalModuleBinds
import com.example.util.simpletimetracker.feature_notification.di.NotificationModule
import com.example.util.simpletimetracker.feature_widget.di.WidgetModule
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module(
    includes = [
        AppModule::class,
        NavigationModule::class,
        DataLocalModule::class,
        DataLocalModuleBinds::class,
        WidgetModule::class,
        NotificationModule::class
    ]
)
interface AppComponent