package com.example.util.simpletimetracker.api

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WebApiModule {
    @Provides
    @Singleton
    fun provideWebApiAdapter(
        wearApi: com.example.util.simpletimetracker.wear_api.WearCommunicationAPI
    ): WebApiAdapter {
        return WebApiAdapter(wearApi)
    }
}