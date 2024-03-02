package com.example.util.simpletimetracker.feature_wear

import com.example.util.simpletimetracker.wear_api.WearCommunicationAPI
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface WidgetModule {

    @Binds
    fun WearCommunicationInteractor.bindWearCommunicationInteractor(): WearCommunicationAPI
}