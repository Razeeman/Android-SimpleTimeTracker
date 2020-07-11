package com.example.util.simpletimetracker.feature_main.di

import com.example.util.simpletimetracker.feature_main.MainFragment
import dagger.Subcomponent

@Subcomponent
interface MainComponent {

    fun inject(fragment: MainFragment)
}