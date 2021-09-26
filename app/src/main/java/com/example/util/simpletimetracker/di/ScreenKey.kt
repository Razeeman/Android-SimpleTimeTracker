package com.example.util.simpletimetracker.di

import com.example.util.simpletimetracker.navigation.params.screen.ScreenParams
import dagger.MapKey
import kotlin.reflect.KClass

@Retention(AnnotationRetention.RUNTIME)
@MapKey
annotation class ScreenKey(val value: KClass<out ScreenParams>)
