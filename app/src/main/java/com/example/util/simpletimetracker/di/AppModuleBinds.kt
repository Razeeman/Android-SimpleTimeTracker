package com.example.util.simpletimetracker.di

import com.example.util.simpletimetracker.core.repo.BaseResourceRepo
import com.example.util.simpletimetracker.core.interactor.GetCurrentDayInteractorImpl
import com.example.util.simpletimetracker.core.interactor.IsSystemInDarkModeInteractorImpl
import com.example.util.simpletimetracker.core.interactor.GetUntrackedRecordsInteractorImpl
import com.example.util.simpletimetracker.core.interactor.IsExportFormatAvailableInteractorImpl
import com.example.util.simpletimetracker.core.mapper.AppColorMapperImpl
import com.example.util.simpletimetracker.core.provider.ApplicationDataProvider
import com.example.util.simpletimetracker.core.provider.CalendarTypeProviderImpl
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.calendar.CalendarTypeProvider
import com.example.util.simpletimetracker.domain.color.mapper.AppColorMapper
import com.example.util.simpletimetracker.domain.daysOfWeek.interactor.GetCurrentDayInteractor
import com.example.util.simpletimetracker.domain.record.interactor.GetUntrackedRecordsInteractor
import com.example.util.simpletimetracker.domain.darkMode.interactor.IsSystemInDarkModeInteractor
import com.example.util.simpletimetracker.domain.fileExport.IsExportFormatAvailableInteractor
import com.example.util.simpletimetracker.provider.ApplicationDataProviderImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface AppModuleBinds {

    @Binds
    fun bindCalendarTypeProvider(impl: CalendarTypeProviderImpl): CalendarTypeProvider

    @Binds
    @Singleton
    fun bindAppColorMapper(impl: AppColorMapperImpl): AppColorMapper

    @Binds
    @Singleton
    fun bindResourceRepo(impl: ResourceRepo): BaseResourceRepo

    @Binds
    @Singleton
    fun bindApplicationDataProvider(impl: ApplicationDataProviderImpl): ApplicationDataProvider

    @Binds
    @Singleton
    fun bindGetUntrackedRecordsInteractor(impl: GetUntrackedRecordsInteractorImpl): GetUntrackedRecordsInteractor

    @Binds
    @Singleton
    fun bindIsSystemInDarkModeInteractor(impl: IsSystemInDarkModeInteractorImpl): IsSystemInDarkModeInteractor

    @Binds
    @Singleton
    fun bindGetCurrentDayInteractor(impl: GetCurrentDayInteractorImpl): GetCurrentDayInteractor

    @Binds
    @Singleton
    fun bindIsExportFormatAvailableInteractor(impl: IsExportFormatAvailableInteractorImpl): IsExportFormatAvailableInteractor
}