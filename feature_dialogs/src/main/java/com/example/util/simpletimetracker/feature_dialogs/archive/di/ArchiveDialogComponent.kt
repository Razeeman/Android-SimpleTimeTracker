package com.example.util.simpletimetracker.feature_dialogs.archive.di

import com.example.util.simpletimetracker.feature_dialogs.archive.view.ArchiveDialogFragment
import dagger.Subcomponent

@Subcomponent
interface ArchiveDialogComponent {

    fun inject(fragment: ArchiveDialogFragment)
}