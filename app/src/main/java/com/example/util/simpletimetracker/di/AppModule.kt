package com.example.util.simpletimetracker.di

import android.content.ContentResolver
import android.content.Context
import com.example.util.simpletimetracker.TimeTrackerApp
import com.example.util.simpletimetracker.core.manager.NotificationManager
import com.example.util.simpletimetracker.domain.di.AppContext
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.RouterImpl
import com.example.util.simpletimetracker.notification.NotificationManagerImpl
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AppModule(application: TimeTrackerApp) {

    private var appContext: Context = application.applicationContext

    @Provides
    @Singleton
    @AppContext
    fun getAppContext(): Context {
        return appContext
    }

    @Provides
    @Singleton
    fun getRouter(routerImpl: RouterImpl): Router {
        return routerImpl
    }

    @Provides
    @Singleton
    fun getNotificationController(notificationControllerImpl: NotificationManagerImpl): NotificationManager {
        return notificationControllerImpl
    }

    @Provides
    @Singleton
    fun getContentResolver(@AppContext context: Context): ContentResolver {
        return context.contentResolver
    }
}