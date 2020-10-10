package com.example.util.simpletimetracker.feature_notification.di

import com.example.util.simpletimetracker.feature_notification.recevier.NotificationReceiver
import dagger.Subcomponent

@Subcomponent
interface NotificationComponent {

    fun inject(receiver: NotificationReceiver)
}