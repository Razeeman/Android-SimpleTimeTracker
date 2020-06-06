package com.example.util.simpletimetracker.feature_change_record_type.di

import com.example.util.simpletimetracker.feature_change_record_type.view.ChangeRecordTypeFragment
import dagger.Subcomponent

@Subcomponent
interface ChangeRecordTypeComponent {

    fun inject(fragment: ChangeRecordTypeFragment)
}