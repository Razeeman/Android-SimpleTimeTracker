package com.example.util.simpletimetracker.feature_tag_selection.di

import com.example.util.simpletimetracker.feature_tag_selection.view.RecordTagSelectionFragment
import dagger.Subcomponent

@Subcomponent
interface RecordTagSelectionComponent {

    fun inject(fragment: RecordTagSelectionFragment)
}