/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.feature_wear

import com.example.util.simpletimetracker.domain.interactor.WearInteractor
import com.example.util.simpletimetracker.wear_api.WearCommunicationAPI
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface WearModule {

    @Binds
    fun WearCommunicationInteractor.bindWearCommunicationInteractor(): WearCommunicationAPI

    // TODO add base flavor noop
    @Binds
    fun WearInteractorImpl.bindWearInteractor(): WearInteractor
}