package com.example.util.simpletimetracker.feature_dialogs.typesFilter.di

import com.example.util.simpletimetracker.feature_dialogs.typesFilter.view.TypesFilterDialogFragment
import dagger.Subcomponent

@Subcomponent
interface TypesFilterComponent {

    fun inject(fragment: TypesFilterDialogFragment)
}