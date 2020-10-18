package com.example.util.simpletimetracker.di

import com.example.util.simpletimetracker.AddRecordTest
import com.example.util.simpletimetracker.AddRecordTypeTest
import com.example.util.simpletimetracker.ChangeRecordTest
import com.example.util.simpletimetracker.ChangeRecordTypeTest
import com.example.util.simpletimetracker.DeleteRecordTest
import com.example.util.simpletimetracker.DeleteRecordTypeTest
import com.example.util.simpletimetracker.MainScreenEmptyTest
import com.example.util.simpletimetracker.MainScreenTest
import com.example.util.simpletimetracker.RecordsRangesTest
import com.example.util.simpletimetracker.StartRecordTest
import com.example.util.simpletimetracker.StatisticsFilterTest
import com.example.util.simpletimetracker.StatisticsRangesTest
import com.example.util.simpletimetracker.StatisticsTest
import com.example.util.simpletimetracker.data_local.di.DataLocalModule
import com.example.util.simpletimetracker.feature_notification.di.NotificationModule
import com.example.util.simpletimetracker.feature_widget.di.WidgetModule
import com.example.util.simpletimetracker.utils.BaseUiTest
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AppModule::class,
        DataLocalModule::class,
        WidgetModule::class,
        NotificationModule::class
    ]
)
interface TestAppComponent : AppComponent {

    fun inject(into: BaseUiTest)
    fun inject(into: MainScreenTest)
    fun inject(into: MainScreenEmptyTest)
    fun inject(into: AddRecordTypeTest)
    fun inject(into: ChangeRecordTypeTest)
    fun inject(into: DeleteRecordTypeTest)
    fun inject(into: AddRecordTest)
    fun inject(into: ChangeRecordTest)
    fun inject(into: DeleteRecordTest)
    fun inject(into: StartRecordTest)
    fun inject(into: RecordsRangesTest)
    fun inject(into: StatisticsTest)
    fun inject(into: StatisticsFilterTest)
    fun inject(into: StatisticsRangesTest)
}