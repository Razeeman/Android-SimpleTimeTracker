package com.example.util.simpletimetracker

import com.example.util.simpletimetracker.wear_api.WearCommunicationAPI
import dagger.BindsOptionalOf
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Wear api is only bound in flavors that include feature_wear,
 * but external record and statistics queries use it in all of them.
 */
@Module
@InstallIn(SingletonComponent::class)
interface OptionalWearApiModule {

    @BindsOptionalOf
    fun optionalWearCommunicationApi(): WearCommunicationAPI
}
