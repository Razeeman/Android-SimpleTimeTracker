package com.example.util.simpletimetracker.feature_notification.di

import com.example.util.simpletimetracker.core.repo.AutomaticBackupRepo
import com.example.util.simpletimetracker.core.repo.AutomaticExportRepo
import com.example.util.simpletimetracker.domain.notifications.interactor.ActivityStartedStoppedBroadcastInteractor
import com.example.util.simpletimetracker.domain.backup.interactor.AutomaticBackupInteractor
import com.example.util.simpletimetracker.domain.backup.interactor.AutomaticExportInteractor
import com.example.util.simpletimetracker.domain.notifications.interactor.NotificationGoalTimeInteractor
import com.example.util.simpletimetracker.domain.notifications.interactor.NotificationInactivityInteractor
import com.example.util.simpletimetracker.domain.notifications.interactor.NotificationActivityInteractor
import com.example.util.simpletimetracker.domain.notifications.interactor.NotificationGoalCountInteractor
import com.example.util.simpletimetracker.domain.notifications.interactor.NotificationGoalRangeEndInteractor
import com.example.util.simpletimetracker.domain.notifications.interactor.NotificationTypeInteractor
import com.example.util.simpletimetracker.domain.notifications.interactor.ScheduledReminderNotificationInteractor
import com.example.util.simpletimetracker.domain.notifications.interactor.NotificationActivitySwitchInteractor
import com.example.util.simpletimetracker.domain.pomodoro.interactor.PomodoroCycleNotificationInteractor
import com.example.util.simpletimetracker.feature_notification.automaticBackup.interactor.AutomaticBackupInteractorImpl
import com.example.util.simpletimetracker.feature_notification.automaticBackup.repo.AutomaticBackupRepoImpl
import com.example.util.simpletimetracker.feature_notification.automaticExport.interactor.AutomaticExportInteractorImpl
import com.example.util.simpletimetracker.feature_notification.automaticExport.repo.AutomaticExportRepoImpl
import com.example.util.simpletimetracker.feature_notification.goalTime.interactor.NotificationGoalTimeInteractorImpl
import com.example.util.simpletimetracker.feature_notification.inactivity.interactor.NotificationInactivityInteractorImpl
import com.example.util.simpletimetracker.feature_notification.activity.interactor.NotificationActivityInteractorImpl
import com.example.util.simpletimetracker.feature_notification.activitySwitch.interactor.NotificationActivitySwitchInteractorImpl
import com.example.util.simpletimetracker.feature_notification.goalTime.interactor.NotificationGoalCountInteractorImpl
import com.example.util.simpletimetracker.feature_notification.goalTime.interactor.NotificationGoalRangeEndInteractorImpl
import com.example.util.simpletimetracker.feature_notification.pomodoro.interactor.PomodoroCycleNotificationInteractorImpl
import com.example.util.simpletimetracker.feature_notification.external.ActivityStartedStoppedBroadcastInteractorImpl
import com.example.util.simpletimetracker.feature_notification.recordType.interactor.NotificationTypeInteractorImpl
import com.example.util.simpletimetracker.feature_notification.scheduledReminder.interactor.ScheduledReminderNotificationInteractorImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface NotificationModule {

    @Binds
    fun bindNotificationTypeInteractor(impl: NotificationTypeInteractorImpl): NotificationTypeInteractor

    @Binds
    fun bindNotificationInactivityInteractor(impl: NotificationInactivityInteractorImpl): NotificationInactivityInteractor

    @Binds
    fun bindNotificationActivityInteractor(impl: NotificationActivityInteractorImpl): NotificationActivityInteractor

    @Binds
    fun bindNotificationGoalTimeInteractor(impl: NotificationGoalTimeInteractorImpl): NotificationGoalTimeInteractor

    @Binds
    fun bindNotificationGoalCountInteractor(impl: NotificationGoalCountInteractorImpl): NotificationGoalCountInteractor

    @Binds
    fun bindNotificationGoalRangeEndInteractor(impl: NotificationGoalRangeEndInteractorImpl): NotificationGoalRangeEndInteractor

    @Binds
    fun bindActivityStartedStoppedBroadcastInteractorImpl(impl: ActivityStartedStoppedBroadcastInteractorImpl): ActivityStartedStoppedBroadcastInteractor

    @Binds
    fun bindAutomaticBackupInteractor(impl: AutomaticBackupInteractorImpl): AutomaticBackupInteractor

    @Binds
    fun bindPomodoroCycleNotificationInteractor(impl: PomodoroCycleNotificationInteractorImpl): PomodoroCycleNotificationInteractor

    @Binds
    fun bindNotificationActivitySwitchInteractor(impl: NotificationActivitySwitchInteractorImpl): NotificationActivitySwitchInteractor

    @Binds
    fun bindAutomaticExportInteractor(impl: AutomaticExportInteractorImpl): AutomaticExportInteractor

    @Binds
    @Singleton
    fun bindAutomaticBackupRepo(impl: AutomaticBackupRepoImpl): AutomaticBackupRepo

    @Binds
    @Singleton
    fun bindAutomaticExportRepo(impl: AutomaticExportRepoImpl): AutomaticExportRepo

    @Binds
    fun bindScheduledReminderNotificationInteractor(impl: ScheduledReminderNotificationInteractorImpl): ScheduledReminderNotificationInteractor
}