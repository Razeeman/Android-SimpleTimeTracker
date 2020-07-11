package com.example.util.simpletimetracker.di

import com.example.util.simpletimetracker.feature_change_record.di.ChangeRecordComponentProvider
import com.example.util.simpletimetracker.feature_change_record_type.di.ChangeRecordTypeComponentProvider
import com.example.util.simpletimetracker.feature_change_running_record.di.ChangeRunningRecordComponentProvider
import com.example.util.simpletimetracker.feature_dialogs.chartFilter.di.ChartFilterComponentProvider
import com.example.util.simpletimetracker.feature_main.di.MainComponentProvider
import com.example.util.simpletimetracker.feature_records.di.RecordsComponentProvider
import com.example.util.simpletimetracker.feature_running_records.di.RunningRecordsComponentProvider
import com.example.util.simpletimetracker.feature_settings.di.SettingsComponentProvider
import com.example.util.simpletimetracker.feature_statistics.di.StatisticsComponentProvider
import com.example.util.simpletimetracker.feature_widget.di.WidgetComponentProvider

interface FeatureComponentProvider :
    MainComponentProvider,
    RunningRecordsComponentProvider,
    ChangeRecordTypeComponentProvider,
    RecordsComponentProvider,
    ChangeRecordComponentProvider,
    ChangeRunningRecordComponentProvider,
    StatisticsComponentProvider,
    SettingsComponentProvider,
    ChartFilterComponentProvider,
    WidgetComponentProvider {

    var appComponent: AppComponent?
}