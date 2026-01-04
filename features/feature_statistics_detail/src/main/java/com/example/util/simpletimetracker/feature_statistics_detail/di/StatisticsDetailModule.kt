package com.example.util.simpletimetracker.feature_statistics_detail.di

import com.example.util.simpletimetracker.feature_statistics_detail.api.StatisticsDetailOptionsListMapper
import com.example.util.simpletimetracker.feature_statistics_detail.mapper.StatisticsDetailOptionsListMapperImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface StatisticsDetailModule {

    @Binds
    fun bindStatisticsDetailOptionsListMapper(impl: StatisticsDetailOptionsListMapperImpl): StatisticsDetailOptionsListMapper
}