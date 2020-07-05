package com.example.util.simpletimetracker.feature_change_running_record.di

import com.example.util.simpletimetracker.feature_change_running_record.view.ChangeRunningRecordFragment
import dagger.Subcomponent

@Subcomponent
interface ChangeRunningRecordComponent {

    fun inject(fragment: ChangeRunningRecordFragment)
}