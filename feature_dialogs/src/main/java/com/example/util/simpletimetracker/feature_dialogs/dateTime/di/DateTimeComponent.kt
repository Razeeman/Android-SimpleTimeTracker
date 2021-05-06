package com.example.util.simpletimetracker.feature_dialogs.dateTime.di

import com.example.util.simpletimetracker.feature_dialogs.dateTime.DateTimeDialogFragment
import dagger.Subcomponent

@Subcomponent
interface DateTimeComponent {

    fun inject(fragment: DateTimeDialogFragment)
}