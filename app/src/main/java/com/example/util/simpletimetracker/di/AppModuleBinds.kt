package com.example.util.simpletimetracker.di

import com.example.util.simpletimetracker.core.mapper.AppColorMapperImpl
import com.example.util.simpletimetracker.domain.mapper.AppColorMapper
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
}