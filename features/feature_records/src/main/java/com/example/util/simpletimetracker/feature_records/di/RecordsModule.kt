package com.example.util.simpletimetracker.feature_records.di

import com.example.util.simpletimetracker.feature_records.api.RecordsContainerOptionsListMapper
import com.example.util.simpletimetracker.feature_records.mapper.RecordsContainerOptionsListMapperImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface RecordsModule {

    @Binds
    fun bindRecordsContainerOptionsListMapper(impl: RecordsContainerOptionsListMapperImpl): RecordsContainerOptionsListMapper
}