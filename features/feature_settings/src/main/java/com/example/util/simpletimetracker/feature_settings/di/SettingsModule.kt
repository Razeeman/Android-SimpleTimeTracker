package com.example.util.simpletimetracker.feature_settings.di

import com.example.util.simpletimetracker.core.model.NavigationTab
import com.example.util.simpletimetracker.core.model.NavigationTabKey
import com.example.util.simpletimetracker.core.model.NavigationTabProvider
import com.example.util.simpletimetracker.feature_settings.api.OnSettingChangedInteractor
import com.example.util.simpletimetracker.feature_settings.api.OnQuickSettingClickInteractor
import com.example.util.simpletimetracker.feature_settings.api.SettingsCardOrderMapper
import com.example.util.simpletimetracker.feature_settings.api.SettingsOrderChangeInteractor
import com.example.util.simpletimetracker.feature_settings.interactor.OnSettingChangedInteractorImpl
import com.example.util.simpletimetracker.feature_settings.interactor.OnQuickSettingClickInteractorImpl
import com.example.util.simpletimetracker.feature_settings.interactor.SettingsOrderChangeInteractorImpl
import com.example.util.simpletimetracker.feature_settings.mapper.SettingsMapper
import com.example.util.simpletimetracker.feature_settings.view.SettingsFragment
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap

@Module
@InstallIn(SingletonComponent::class)
interface SettingsModule {

    @Binds
    fun bindSettingsCardOrderMapper(impl: SettingsMapper): SettingsCardOrderMapper

    @Binds
    fun bindOnSettingChangedInteractor(impl: OnSettingChangedInteractorImpl): OnSettingChangedInteractor

    @Binds
    fun bindOnQuickSettingClickInteractor(impl: OnQuickSettingClickInteractorImpl): OnQuickSettingClickInteractor

    @Binds
    fun bindSettingsOrderChangeInteractor(impl: SettingsOrderChangeInteractorImpl): SettingsOrderChangeInteractor

    companion object {
        @Provides
        @IntoMap
        @NavigationTabKey(NavigationTab.Settings::class)
        fun bindNavigationTab(): NavigationTabProvider {
            return NavigationTabProvider { SettingsFragment.newInstance() }
        }
    }
}