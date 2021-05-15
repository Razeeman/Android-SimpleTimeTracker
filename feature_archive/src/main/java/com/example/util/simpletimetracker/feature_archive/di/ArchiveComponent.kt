package com.example.util.simpletimetracker.feature_archive.di

import com.example.util.simpletimetracker.feature_archive.view.ArchiveFragment
import dagger.Subcomponent

@Subcomponent
interface ArchiveComponent {

    fun inject(fragment: ArchiveFragment)
}