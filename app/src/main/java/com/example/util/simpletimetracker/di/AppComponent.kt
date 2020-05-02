package com.example.util.simpletimetracker.di

import com.example.util.simpletimetracker.ui.MainViewModel
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class])
interface AppComponent {

    fun inject(viewModel: MainViewModel)
}