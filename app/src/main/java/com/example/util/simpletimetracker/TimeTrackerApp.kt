package com.example.util.simpletimetracker

import android.app.Application
import android.os.StrictMode
import androidx.emoji.bundled.BundledEmojiCompatConfig
import androidx.emoji.text.EmojiCompat
import com.example.util.simpletimetracker.di.AppComponent
import com.example.util.simpletimetracker.di.AppModule
import com.example.util.simpletimetracker.di.DaggerAppComponent
import com.example.util.simpletimetracker.di.FeatureComponentProvider
import com.example.util.simpletimetracker.feature_archive.di.ArchiveComponent
import com.example.util.simpletimetracker.feature_categories.di.CategoriesComponent
import com.example.util.simpletimetracker.feature_change_category.di.ChangeCategoryComponent
import com.example.util.simpletimetracker.feature_change_record.di.ChangeRecordComponent
import com.example.util.simpletimetracker.feature_change_record_tag.di.ChangeRecordTagComponent
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
import com.example.util.simpletimetracker.feature_records.di.RecordsComponent
import com.example.util.simpletimetracker.feature_records_all.di.RecordsAllComponent
import com.example.util.simpletimetracker.feature_running_records.di.RunningRecordsComponent
import com.example.util.simpletimetracker.feature_settings.di.SettingsComponent
import com.example.util.simpletimetracker.feature_statistics.di.StatisticsComponent
import com.example.util.simpletimetracker.feature_statistics_detail.di.StatisticsDetailComponent
import com.example.util.simpletimetracker.feature_widget.di.WidgetComponent
import timber.log.Timber
import timber.log.Timber.DebugTree

class TimeTrackerApp : Application(), FeatureComponentProvider {

    override var appComponent: AppComponent? = null
    override var mainComponent: MainComponent? = null
    override var runningRecordsComponent: RunningRecordsComponent? = null
    override var changeRecordTypeComponent: ChangeRecordTypeComponent? = null
    override var recordsComponent: RecordsComponent? = null
    override var recordsAllComponent: RecordsAllComponent? = null
    override var changeRecordComponent: ChangeRecordComponent? = null
    override var changeRunningRecordComponent: ChangeRunningRecordComponent? = null
    override var statisticsComponent: StatisticsComponent? = null
    override var statisticsDetailComponent: StatisticsDetailComponent? = null
    override var settingsComponent: SettingsComponent? = null
    override var chartFilterComponent: ChartFilterComponent? = null
    override var typesFilterComponent: TypesFilterComponent? = null
    override var durationPickerComponent: DurationPickerComponent? = null
    override var cardSizeComponent: CardSizeComponent? = null
    override var cardOrderComponent: CardOrderComponent? = null
    override var dateTimeComponent: DateTimeComponent? = null
    override var emojiSelectionComponent: EmojiSelectionComponent? = null
    override var widgetComponent: WidgetComponent? = null
    override var notificationComponent: NotificationComponent? = null
    override var categoriesComponent: CategoriesComponent? = null
    override var changeCategoryComponent: ChangeCategoryComponent? = null
    override var changeRecordTagComponent: ChangeRecordTagComponent? = null
    override var archiveComponent: ArchiveComponent? = null

    override fun onCreate() {
        super.onCreate()
        initLog()
        initDi()
        initLibraries()
        initStrictMode()
    }

    private fun initLog() {
        if (BuildConfig.DEBUG) {
            Timber.plant(DebugTree())
        }
    }

    private fun initDi() {
        appComponent = DaggerAppComponent
            .builder()
            .appModule(AppModule(this))
            .build()
        mainComponent = appComponent?.plusMainComponent()
        runningRecordsComponent = appComponent?.plusRunningRecordsComponent()
        changeRecordTypeComponent = appComponent?.plusChangeRecordTypeComponent()
        recordsComponent = appComponent?.plusRecordsComponent()
        recordsAllComponent = appComponent?.plusRecordsAllComponent()
        changeRecordComponent = appComponent?.plusChangeRecordComponent()
        changeRunningRecordComponent = appComponent?.plusChangeRunningRecordComponent()
        statisticsComponent = appComponent?.plusStatisticsComponent()
        statisticsDetailComponent = appComponent?.plusStatisticsDetailComponent()
        settingsComponent = appComponent?.plusSettingComponent()
        chartFilterComponent = appComponent?.plusChartFilterComponent()
        typesFilterComponent = appComponent?.plusTypesFilterComponent()
        durationPickerComponent = appComponent?.plusDurationPickerComponent()
        cardSizeComponent = appComponent?.plusCardSizeComponent()
        cardOrderComponent = appComponent?.plusCardOrderComponent()
        dateTimeComponent = appComponent?.plusDateTimeComponent()
        emojiSelectionComponent = appComponent?.plusEmojiSelectionComponent()
        widgetComponent = appComponent?.plusWidgetComponent()
        notificationComponent = appComponent?.plusNotificationComponent()
        categoriesComponent = appComponent?.plusCategoriesComponent()
        changeCategoryComponent = appComponent?.plusChangeCategoryComponent()
        changeRecordTagComponent = appComponent?.plusChangeRecordTagComponent()
        archiveComponent = appComponent?.plusArchiveComponent()
    }

    private fun initLibraries() {
        val config = BundledEmojiCompatConfig(applicationContext)
            .setReplaceAll(true)
        EmojiCompat.init(config)
    }

    private fun initStrictMode() {
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .penaltyDialog()
                    .build()
            )
            StrictMode.setVmPolicy(
                StrictMode.VmPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build()
            )
        }
    }
}