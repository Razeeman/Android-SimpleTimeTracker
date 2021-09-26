package com.example.util.simpletimetracker.di

import com.example.util.simpletimetracker.navigation.Screen
import dagger.MapKey

@Retention(AnnotationRetention.RUNTIME)
@MapKey
annotation class ScreenKey(val value: Screen)
