package com.example.util.simpletimetracker.di

import com.example.util.simpletimetracker.AddRecordTypeTest
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

    fun inject(mainScreenTest: MainScreenTest)
    fun inject(addRecordTypeTest: AddRecordTypeTest)
}