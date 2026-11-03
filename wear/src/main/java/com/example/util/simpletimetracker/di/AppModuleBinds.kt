/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.di

import com.example.util.simpletimetracker.core.repo.BaseResourceRepo
import com.example.util.simpletimetracker.data.WearResourceRepo
import com.example.util.simpletimetracker.domain.calendar.CalendarTypeProvider
import com.example.util.simpletimetracker.provider.WearCalendarTypeProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface AppModuleBinds {

    @Binds
    fun bindCalendarTypeProvider(impl: WearCalendarTypeProvider): CalendarTypeProvider

    @Binds
    @Singleton
    fun bindResourceRepo(impl: WearResourceRepo): BaseResourceRepo
}