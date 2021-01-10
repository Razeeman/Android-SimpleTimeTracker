package com.example.util.simpletimetracker.feature_dialogs.duration.di

import com.example.util.simpletimetracker.feature_dialogs.duration.view.DurationDialogFragment
import dagger.Subcomponent

@Subcomponent
interface DurationPickerComponent {

    fun inject(fragment: DurationDialogFragment)
}