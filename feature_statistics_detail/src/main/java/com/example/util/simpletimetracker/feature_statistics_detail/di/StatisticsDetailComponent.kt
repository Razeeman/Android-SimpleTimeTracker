package com.example.util.simpletimetracker.feature_statistics_detail.di

import com.example.util.simpletimetracker.feature_statistics_detail.view.StatisticsDetailFragment
import dagger.Subcomponent

@Subcomponent
interface StatisticsDetailComponent {

    fun inject(fragment: StatisticsDetailFragment)
}