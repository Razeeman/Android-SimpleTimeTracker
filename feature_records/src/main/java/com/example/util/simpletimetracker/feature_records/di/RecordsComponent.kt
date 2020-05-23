package com.example.util.simpletimetracker.feature_records.di

import com.example.util.simpletimetracker.feature_records.view.RecordsContainerFragment
import com.example.util.simpletimetracker.feature_records.view.RecordsFragment
import dagger.Subcomponent

@Subcomponent
interface RecordsComponent {

    fun inject(fragment: RecordsFragment)
    fun inject(fragment: RecordsContainerFragment)
}