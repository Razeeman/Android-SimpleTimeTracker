package com.example.util.simpletimetracker.di

import com.example.util.simpletimetracker.data_local.di.DataLocalModule
import com.example.util.simpletimetracker.feature_categories.di.CategoriesComponent
import com.example.util.simpletimetracker.feature_change_category.di.ChangeCategoryComponent
import com.example.util.simpletimetracker.feature_change_record.di.ChangeRecordComponent
import com.example.util.simpletimetracker.feature_change_record_type.di.ChangeRecordTypeComponent
import com.example.util.simpletimetracker.feature_change_running_record.di.ChangeRunningRecordComponent
import com.example.util.simpletimetracker.feature_dialogs.cardOrder.di.CardOrderComponent
import com.example.util.simpletimetracker.feature_dialogs.cardSize.di.CardSizeComponent
import com.example.util.simpletimetracker.feature_dialogs.chartFilter.di.ChartFilterComponent
import com.example.util.simpletimetracker.feature_dialogs.dateTime.di.DateTimeComponent
import com.example.util.simpletimetracker.feature_dialogs.duration.di.DurationPickerComponent
import com.example.util.simpletimetracker.feature_dialogs.emojiSelection.di.EmojiSelectionComponent
import com.example.util.simpletimetracker.feature_dialogs.typesFilter.di.TypesFilterComponent
import com.example.util.simpletimetracker.feature_main.di.MainComponent
import com.example.util.simpletimetracker.feature_notification.di.NotificationComponent
import com.example.util.simpletimetracker.feature_notification.di.NotificationModule
import com.example.util.simpletimetracker.feature_records.di.RecordsComponent
import com.example.util.simpletimetracker.feature_records_all.di.RecordsAllComponent
import com.example.util.simpletimetracker.feature_running_records.di.RunningRecordsComponent
import com.example.util.simpletimetracker.feature_settings.di.SettingsComponent
import com.example.util.simpletimetracker.feature_statistics.di.StatisticsComponent
import com.example.util.simpletimetracker.feature_statistics_detail.di.StatisticsDetailComponent
import com.example.util.simpletimetracker.feature_widget.di.WidgetComponent
import com.example.util.simpletimetracker.feature_widget.di.WidgetModule
import com.example.util.simpletimetracker.ui.MainActivity
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AppModule::class,
        NavigationModule::class,
        DataLocalModule::class,
        WidgetModule::class,
        NotificationModule::class
    ]
)
interface AppComponent {

    fun inject(mainActivity: MainActivity)

    fun plusMainComponent(): MainComponent
    fun plusRunningRecordsComponent(): RunningRecordsComponent
    fun plusChangeRecordTypeComponent(): ChangeRecordTypeComponent
    fun plusRecordsComponent(): RecordsComponent
    fun plusRecordsAllComponent(): RecordsAllComponent
    fun plusChangeRecordComponent(): ChangeRecordComponent
    fun plusChangeRunningRecordComponent(): ChangeRunningRecordComponent
    fun plusStatisticsComponent(): StatisticsComponent
    fun plusStatisticsDetailComponent(): StatisticsDetailComponent
    fun plusSettingComponent(): SettingsComponent
    fun plusChartFilterComponent(): ChartFilterComponent
    fun plusTypesFilterComponent(): TypesFilterComponent
    fun plusDurationPickerComponent(): DurationPickerComponent
    fun plusCardSizeComponent(): CardSizeComponent
    fun plusCardOrderComponent(): CardOrderComponent
    fun plusDateTimeComponent(): DateTimeComponent
    fun plusEmojiSelectionComponent(): EmojiSelectionComponent
    fun plusWidgetComponent(): WidgetComponent
    fun plusNotificationComponent(): NotificationComponent
    fun plusCategoriesComponent(): CategoriesComponent
    fun plusChangeCategoryComponent(): ChangeCategoryComponent
}