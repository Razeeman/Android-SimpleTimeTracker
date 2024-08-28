package com.example.util.simpletimetracker.feature_change_goals.di

import com.example.util.simpletimetracker.feature_change_goals.api.GoalsViewModelDelegate
import com.example.util.simpletimetracker.feature_change_goals.delegate.GoalsViewModelDelegateImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface ChangeGoalsModule {

    @Binds
    fun bindGoalsViewModelDelegate(impl: GoalsViewModelDelegateImpl): GoalsViewModelDelegate
}