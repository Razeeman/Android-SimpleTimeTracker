package com.example.util.simpletimetracker.feature_statistics.di

import com.example.util.simpletimetracker.feature_statistics.viewModel.StatisticsContainerViewModel
import com.example.util.simpletimetracker.feature_statistics.viewModel.StatisticsViewModel
import dagger.Subcomponent

@Subcomponent
interface StatisticsComponent {

    fun inject(viewModel: StatisticsViewModel)
    fun inject(viewModel: StatisticsContainerViewModel)
}