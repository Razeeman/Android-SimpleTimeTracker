package com.example.util.simpletimetracker.feature_categories.di

import com.example.util.simpletimetracker.feature_categories.view.CategoriesFragment
import dagger.Subcomponent

@Subcomponent
interface CategoriesComponent {

    fun inject(fragment: CategoriesFragment)
}