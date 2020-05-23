package com.example.util.simpletimetracker.feature_change_record.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.util.simpletimetracker.feature_change_record.viewModel.ChangeRecordViewModel
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import javax.inject.Provider

@Module
class ChangeRecordModule {

    @Provides
    @IntoMap
    @ViewModelKey(ChangeRecordViewModel::class)
    fun provideChangeRecordViewModel(viewModel: ChangeRecordViewModel): ViewModel {
        return viewModel
    }

    @Provides
    fun provideViewModelFactory(
        providers: Map<Class<out ViewModel>, @JvmSuppressWildcards Provider<ViewModel>>
    ): ViewModelProvider.Factory {
        return ViewModelProviderFactory(
            providers
        )
    }
}