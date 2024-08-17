package com.example.util.simpletimetracker

import com.example.util.simpletimetracker.domain.interactor.WearInteractor
import com.example.util.simpletimetracker.feature_wear.WearInteractorImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface WearModule {

    @Binds
    fun bindWearInteractor(impl: WearInteractorImpl): WearInteractor
}