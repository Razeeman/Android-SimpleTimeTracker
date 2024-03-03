/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.di

import com.example.util.simpletimetracker.data.ContextMessenger
import com.example.util.simpletimetracker.data.Messenger
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface WearModule {

    @Binds
    fun ContextMessenger.bindMessenger(): Messenger
}