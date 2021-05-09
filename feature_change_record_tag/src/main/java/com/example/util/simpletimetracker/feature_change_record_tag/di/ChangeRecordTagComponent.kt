package com.example.util.simpletimetracker.feature_change_record_tag.di

import com.example.util.simpletimetracker.feature_change_record_tag.view.ChangeRecordTagFragment
import dagger.Subcomponent

@Subcomponent
interface ChangeRecordTagComponent {

    fun inject(fragment: ChangeRecordTagFragment)
}