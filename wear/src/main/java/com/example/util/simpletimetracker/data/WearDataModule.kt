/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.data

import android.content.Context
import android.content.SharedPreferences
import com.example.util.simpletimetracker.domain.repo.WearPrefsRepo
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface WearDataModule {

    @Binds
    @Singleton
    fun bindPrefsRepo(impl: WearPrefsRepoImpl): WearPrefsRepo

    companion object {
        private const val PREFS_NAME = "prefs_simple_time_tracker_wear"

        @Provides
        @Singleton
        fun getSharedPrefs(@ApplicationContext context: Context): SharedPreferences {
            return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
    }
}