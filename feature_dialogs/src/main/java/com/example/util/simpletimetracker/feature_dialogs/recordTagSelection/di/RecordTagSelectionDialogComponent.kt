package com.example.util.simpletimetracker.feature_dialogs.recordTagSelection.di

import com.example.util.simpletimetracker.feature_dialogs.recordTagSelection.view.RecordTagSelectionDialogFragment
import dagger.Subcomponent

@Subcomponent
interface RecordTagSelectionDialogComponent {

    fun inject(fragment: RecordTagSelectionDialogFragment)
}