package com.example.util.simpletimetracker.feature_notification.di

import com.example.util.simpletimetracker.core.repo.AutomaticBackupRepo
import com.example.util.simpletimetracker.core.repo.AutomaticExportRepo
import com.example.util.simpletimetracker.domain.interactor.ActivityStartedStoppedBroadcastInteractor
import com.example.util.simpletimetracker.domain.interactor.AutomaticBackupInteractor
import com.example.util.simpletimetracker.domain.interactor.AutomaticExportInteractor
import com.example.util.simpletimetracker.domain.interactor.NotificationGoalTimeInteractor
import com.example.util.simpletimetracker.domain.interactor.NotificationInactivityInteractor
import com.example.util.simpletimetracker.domain.interactor.NotificationActivityInteractor
import com.example.util.simpletimetracker.domain.interactor.NotificationGoalCountInteractor
import com.example.util.simpletimetracker.domain.interactor.NotificationTypeInteractor
import com.example.util.simpletimetracker.feature_notification.automaticBackup.interactor.AutomaticBackupInteractorImpl
import com.example.util.simpletimetracker.feature_notification.automaticBackup.repo.AutomaticBackupRepoImpl
import com.example.util.simpletimetracker.feature_notification.automaticExport.interactor.AutomaticExportInteractorImpl
import com.example.util.simpletimetracker.feature_notification.automaticExport.repo.AutomaticExportRepoImpl
import com.example.util.simpletimetracker.feature_notification.goalTime.interactor.NotificationGoalTimeInteractorImpl
import com.example.util.simpletimetracker.feature_notification.inactivity.interactor.NotificationInactivityInteractorImpl
import com.example.util.simpletimetracker.feature_notification.activity.interactor.NotificationActivityInteractorImpl
import com.example.util.simpletimetracker.feature_notification.goalTime.interactor.NotificationGoalCountInteractorImpl
import com.example.util.simpletimetracker.feature_notification.recordType.interactor.ActivityStartedStoppedBroadcastInteractorImpl
import com.example.util.simpletimetracker.feature_notification.recordType.interactor.NotificationTypeInteractorImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface NotificationModule {

    @Binds
    fun NotificationTypeInteractorImpl.bindNotificationTypeInteractor(): NotificationTypeInteractor

    @Binds
    fun NotificationInactivityInteractorImpl.bindNotificationInactivityInteractor(): NotificationInactivityInteractor

    @Binds
    fun NotificationActivityInteractorImpl.bindNotificationActivityInteractor(): NotificationActivityInteractor

    @Binds
    fun NotificationGoalTimeInteractorImpl.bindNotificationGoalTimeInteractor(): NotificationGoalTimeInteractor

    @Binds
    fun NotificationGoalCountInteractorImpl.bindNotificationGoalCountInteractor(): NotificationGoalCountInteractor

    @Binds
    fun ActivityStartedStoppedBroadcastInteractorImpl.bindActivityStartedStoppedBroadcastInteractorImpl(): ActivityStartedStoppedBroadcastInteractor

    @Binds
    fun AutomaticBackupInteractorImpl.bindAutomaticBackupInteractor(): AutomaticBackupInteractor

    @Binds
    @Singleton
    fun AutomaticBackupRepoImpl.bindAutomaticBackupRepo(): AutomaticBackupRepo

    @Binds
    fun AutomaticExportInteractorImpl.bindAutomaticExportInteractor(): AutomaticExportInteractor

    @Binds
    @Singleton
    fun AutomaticExportRepoImpl.bindAutomaticExportRepo(): AutomaticExportRepo
}