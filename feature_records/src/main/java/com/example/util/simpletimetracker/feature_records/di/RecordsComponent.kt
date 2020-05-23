package com.example.util.simpletimetracker.feature_records.di

import com.example.util.simpletimetracker.feature_records.viewModel.RecordsContainerViewModel
import com.example.util.simpletimetracker.feature_records.viewModel.RecordsViewModel
import dagger.Subcomponent

@Subcomponent
interface RecordsComponent {

    fun inject(viewModel: RecordsViewModel)
    fun inject(viewModel: RecordsContainerViewModel)
}