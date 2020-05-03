package com.example.util.simpletimetracker.di

import com.example.util.simpletimetracker.data_local.di.DataLocalModule
import com.example.util.simpletimetracker.ui.RunningRecordsViewModel
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AppModule::class,
        DataLocalModule::class]
)
interface AppComponent {

    fun inject(viewModel: RunningRecordsViewModel)
}