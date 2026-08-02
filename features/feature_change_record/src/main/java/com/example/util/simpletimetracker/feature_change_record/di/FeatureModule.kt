package com.example.util.simpletimetracker.feature_change_record.di

import com.example.util.simpletimetracker.feature_change_record.viewModel.ChangeRecordEditorDelegateImpl
import com.example.util.simpletimetracker.feature_change_record.api.ChangeRecordEditorDelegate
import com.example.util.simpletimetracker.feature_change_record.api.view.ChangeRecordViewDelegateProvider
import com.example.util.simpletimetracker.feature_change_record.view.ChangeRecordViewDelegateProviderImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface FeatureModule {

    @Binds
    fun bindChangeRecordEditorDelegate(impl: ChangeRecordEditorDelegateImpl): ChangeRecordEditorDelegate

    @Binds
    fun bindChangeRecordViewDelegateProvider(impl: ChangeRecordViewDelegateProviderImpl): ChangeRecordViewDelegateProvider
}