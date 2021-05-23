package com.example.util.simpletimetracker.di

import com.example.util.simpletimetracker.navigation.ActionResolver
import com.example.util.simpletimetracker.navigation.ActionResolverImpl
import com.example.util.simpletimetracker.navigation.NotificationResolver
import com.example.util.simpletimetracker.navigation.NotificationResolverImpl
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.RouterImpl
import com.example.util.simpletimetracker.navigation.ScreenFactory
import com.example.util.simpletimetracker.navigation.ScreenFactoryImpl
import com.example.util.simpletimetracker.navigation.ScreenResolver
import com.example.util.simpletimetracker.navigation.ScreenResolverImpl
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class NavigationModule {

    @Provides
    @Singleton
    fun getScreenResolver(screenResolverImpl: ScreenResolverImpl): ScreenResolver {
        return screenResolverImpl
    }

    @Provides
    @Singleton
    fun getScreenFactory(screenFactoryImpl: ScreenFactoryImpl): ScreenFactory {
        return screenFactoryImpl
    }

    @Provides
    @Singleton
    fun getActionResolver(actionResolverImpl: ActionResolverImpl): ActionResolver {
        return actionResolverImpl
    }

    @Provides
    @Singleton
    fun getNotificationResolver(notificationResolverImpl: NotificationResolverImpl): NotificationResolver {
        return notificationResolverImpl
    }

    @Provides
    @Singleton
    fun getRouter(routerImpl: RouterImpl): Router {
        return routerImpl
    }
}