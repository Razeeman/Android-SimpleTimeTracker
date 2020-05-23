package com.example.util.simpletimetracker.feature_statistics.di

import com.example.util.simpletimetracker.feature_statistics.view.StatisticsContainerFragment
import com.example.util.simpletimetracker.feature_statistics.view.StatisticsFragment
import dagger.Subcomponent

@Subcomponent
interface StatisticsComponent {

    fun inject(fragment: StatisticsFragment)
    fun inject(fragment: StatisticsContainerFragment)
}