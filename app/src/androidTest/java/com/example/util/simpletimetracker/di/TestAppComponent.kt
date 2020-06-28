package com.example.util.simpletimetracker.di

import com.example.util.simpletimetracker.AddRecordTest
import com.example.util.simpletimetracker.AddRecordTypeTest
import com.example.util.simpletimetracker.ChangeRecordTest
import com.example.util.simpletimetracker.ChangeRecordTypeTest
import com.example.util.simpletimetracker.DeleteRecordTest
import com.example.util.simpletimetracker.DeleteRecordTypeTest
import com.example.util.simpletimetracker.MainScreenTest
import com.example.util.simpletimetracker.data_local.di.DataLocalModule
import com.example.util.simpletimetracker.feature_widget.di.WidgetModule
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AppModule::class,
        DataLocalModule::class,
        WidgetModule::class
    ]
)
interface TestAppComponent: AppComponent {

    fun inject(into: MainScreenTest)
    fun inject(into: AddRecordTypeTest)
    fun inject(into: ChangeRecordTypeTest)
    fun inject(into: DeleteRecordTypeTest)
    fun inject(into: AddRecordTest)
    fun inject(into: ChangeRecordTest)
    fun inject(into: DeleteRecordTest)
}