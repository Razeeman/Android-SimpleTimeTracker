package com.example.util.simpletimetracker.feature_records_all.di

import com.example.util.simpletimetracker.feature_records_all.RecordsAllFragment
import dagger.Subcomponent

@Subcomponent
interface RecordsAllComponent {

    fun inject(fragment: RecordsAllFragment)
}