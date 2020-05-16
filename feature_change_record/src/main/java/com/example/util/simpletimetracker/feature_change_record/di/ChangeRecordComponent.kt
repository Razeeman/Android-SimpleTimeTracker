package com.example.util.simpletimetracker.feature_change_record.di

import com.example.util.simpletimetracker.feature_change_record.viewModel.ChangeRecordViewModel
import dagger.Subcomponent

@Subcomponent
interface ChangeRecordComponent {

    fun inject(viewModel: ChangeRecordViewModel)
}