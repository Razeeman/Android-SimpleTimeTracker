package com.example.util.simpletimetracker.di

import com.example.util.simpletimetracker.feature_change_record.di.ChangeRecordComponentProvider
import com.example.util.simpletimetracker.feature_change_record_type.di.ChangeRecordTypeComponentProvider
import com.example.util.simpletimetracker.feature_change_running_record.di.ChangeRunningRecordComponentProvider
import com.example.util.simpletimetracker.feature_dialogs.cardOrder.di.CardOrderComponentProvider
import com.example.util.simpletimetracker.feature_dialogs.cardSize.di.CardSizeComponentProvider
import com.example.util.simpletimetracker.feature_dialogs.chartFilter.di.ChartFilterComponentProvider
import com.example.util.simpletimetracker.feature_main.di.MainComponentProvider
import com.example.util.simpletimetracker.feature_notification.di.NotificationComponentProvider
import com.example.util.simpletimetracker.feature_records.di.RecordsComponentProvider
import com.example.util.simpletimetracker.feature_records_all.di.RecordsAllComponentProvider
import com.example.util.simpletimetracker.feature_running_records.di.RunningRecordsComponentProvider
import com.example.util.simpletimetracker.feature_settings.di.SettingsComponentProvider
import com.example.util.simpletimetracker.feature_statistics.di.StatisticsComponentProvider
import com.example.util.simpletimetracker.feature_statistics_detail.di.StatisticsDetailComponentProvider
import com.example.util.simpletimetracker.feature_widget.di.WidgetComponentProvider

interface FeatureComponentProvider :
    MainComponentProvider,
    RunningRecordsComponentProvider,
    ChangeRecordTypeComponentProvider,
    RecordsComponentProvider,
    RecordsAllComponentProvider,
    ChangeRecordComponentProvider,
    ChangeRunningRecordComponentProvider,
    StatisticsComponentProvider,
    StatisticsDetailComponentProvider,
    SettingsComponentProvider,
    ChartFilterComponentProvider,
    CardSizeComponentProvider,
    CardOrderComponentProvider,
    WidgetComponentProvider,
    NotificationComponentProvider {

    var appComponent: AppComponent?
}