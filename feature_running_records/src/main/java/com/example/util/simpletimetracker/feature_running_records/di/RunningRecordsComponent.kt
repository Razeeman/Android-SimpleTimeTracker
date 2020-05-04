package com.example.util.simpletimetracker.feature_running_records.di

import com.example.util.simpletimetracker.feature_running_records.viewModel.RunningRecordsViewModel
import dagger.Subcomponent

@Subcomponent
interface RunningRecordsComponent {

    fun inject(viewModel: RunningRecordsViewModel)
}