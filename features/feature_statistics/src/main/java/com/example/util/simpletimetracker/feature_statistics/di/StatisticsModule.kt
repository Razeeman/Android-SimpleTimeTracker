package com.example.util.simpletimetracker.feature_statistics.di

import com.example.util.simpletimetracker.feature_statistics.api.StatisticsContainerOptionsListMapper
import com.example.util.simpletimetracker.feature_statistics.mapper.StatisticsContainerOptionsListMapperImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface StatisticsModule {

    @Binds
    fun bindStatisticsContainerOptionsListMapper(impl: StatisticsContainerOptionsListMapperImpl): StatisticsContainerOptionsListMapper
}