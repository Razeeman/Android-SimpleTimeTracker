package com.example.util.simpletimetracker.feature_records.di

import com.example.util.simpletimetracker.feature_records.di.RecordsComponent

interface RecordsComponentProvider {

    fun provideRecordsComponent(): RecordsComponent?
}