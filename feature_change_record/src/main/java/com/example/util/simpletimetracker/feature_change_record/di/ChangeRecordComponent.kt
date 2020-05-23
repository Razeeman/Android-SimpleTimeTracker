package com.example.util.simpletimetracker.feature_change_record.di

import com.example.util.simpletimetracker.feature_change_record.view.ChangeRecordFragment
import dagger.Subcomponent

@Subcomponent(modules = [ChangeRecordModule::class])
interface ChangeRecordComponent {

    fun inject(fragment: ChangeRecordFragment)
}