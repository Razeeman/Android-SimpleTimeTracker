package com.example.util.simpletimetracker.di

import com.example.util.simpletimetracker.core.interactor.GetCurrentDayInteractorImpl
import com.example.util.simpletimetracker.core.interactor.IsSystemInDarkModeInteractorImpl
import com.example.util.simpletimetracker.core.interactor.GetUntrackedRecordsInteractorImpl
import com.example.util.simpletimetracker.core.mapper.AppColorMapperImpl
import com.example.util.simpletimetracker.core.provider.ApplicationDataProvider
import com.example.util.simpletimetracker.domain.interactor.GetCurrentDayInteractor
import com.example.util.simpletimetracker.domain.interactor.IsSystemInDarkModeInteractor
import com.example.util.simpletimetracker.domain.interactor.GetUntrackedRecordsInteractor
import com.example.util.simpletimetracker.domain.mapper.AppColorMapper
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
    @Singleton
    fun bindAppColorMapper(impl: AppColorMapperImpl): AppColorMapper

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
}