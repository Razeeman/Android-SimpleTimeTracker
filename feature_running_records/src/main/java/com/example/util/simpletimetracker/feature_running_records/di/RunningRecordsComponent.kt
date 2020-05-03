package com.example.util.simpletimetracker.feature_running_records.di

import com.example.util.simpletimetracker.feature_running_records.RunningRecordsViewModel
import dagger.Subcomponent

@Subcomponent
interface RunningRecordsComponent {

    fun inject(viewModel: RunningRecordsViewModel)
}