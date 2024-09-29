package com.example.util.simpletimetracker.di

import com.example.util.simpletimetracker.navigation.NotificationResolver
import com.example.util.simpletimetracker.navigation.NotificationResolverImpl
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.RouterImpl
import com.example.util.simpletimetracker.navigation.ScreenFactory
import com.example.util.simpletimetracker.navigation.ScreenFactoryImpl
import com.example.util.simpletimetracker.navigation.ScreenResolver
import com.example.util.simpletimetracker.navigation.ScreenResolverImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface NavigationModule {

    @Binds
    @Singleton
    fun bindScreenResolver(impl: ScreenResolverImpl): ScreenResolver

    @Binds
    @Singleton
    fun bindScreenFactory(impl: ScreenFactoryImpl): ScreenFactory

    @Binds
    @Singleton
    fun bindNotificationResolver(impl: NotificationResolverImpl): NotificationResolver

    @Binds
    @Singleton
    fun bindRouter(impl: RouterImpl): Router
}