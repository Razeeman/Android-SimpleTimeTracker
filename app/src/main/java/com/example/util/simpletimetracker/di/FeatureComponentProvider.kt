package com.example.util.simpletimetracker.di

import com.example.util.simpletimetracker.feature_change_record_type.di.ChangeRecordTypeComponentProvider
import com.example.util.simpletimetracker.feature_records.di.RecordsComponentProvider
import com.example.util.simpletimetracker.feature_running_records.di.RunningRecordsComponentProvider

interface FeatureComponentProvider :
    RunningRecordsComponentProvider,
    RecordsComponentProvider,
    ChangeRecordTypeComponentProvider {

    var appComponent: AppComponent?
}