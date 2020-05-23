package com.example.util.simpletimetracker.feature_dialogs.chartFilter.di

import com.example.util.simpletimetracker.feature_dialogs.chartFilter.view.ChartFilterDialogFragment
import dagger.Subcomponent

@Subcomponent
interface ChartFilterComponent {

    fun inject(fragment: ChartFilterDialogFragment)
}