package com.example.util.simpletimetracker.feature_change_category.di

import com.example.util.simpletimetracker.feature_change_category.view.ChangeCategoryFragment
import dagger.Subcomponent

@Subcomponent
interface ChangeCategoryComponent {

    fun inject(fragment: ChangeCategoryFragment)
}