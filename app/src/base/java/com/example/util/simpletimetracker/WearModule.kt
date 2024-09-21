package com.example.util.simpletimetracker

import com.example.util.simpletimetracker.domain.interactor.WearInteractor
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject

@Module
@InstallIn(SingletonComponent::class)
interface WearModule {

    @Binds
    fun bindWearInteractor(impl: NoopWearInteractor): WearInteractor

    class NoopWearInteractor @Inject constructor() : WearInteractor {
        override suspend fun update() {
            // DO nothing.
        }
    }
}