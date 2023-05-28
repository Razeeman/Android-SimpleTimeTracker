package com.example.util.simpletimetracker.di

import com.example.util.simpletimetracker.core.interactor.GetUntrackedRecordsInteractorImpl
import com.example.util.simpletimetracker.core.mapper.AppColorMapperImpl
import com.example.util.simpletimetracker.core.provider.ApplicationDataProvider
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
    fun AppColorMapperImpl.bindAppColorMapper(): AppColorMapper

    @Binds
    @Singleton
    fun ApplicationDataProviderImpl.bindApplicationDataProvider(): ApplicationDataProvider

    @Binds
    @Singleton
    fun GetUntrackedRecordsInteractorImpl.bindGetUntrackedRecordsInteractor(): GetUntrackedRecordsInteractor
}