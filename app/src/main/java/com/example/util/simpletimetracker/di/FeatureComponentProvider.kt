package com.example.util.simpletimetracker.di

import com.example.util.simpletimetracker.feature_records.di.RecordsComponentProvider
import com.example.util.simpletimetracker.feature_running_records.di.RunningRecordsComponentProvider

interface FeatureComponentProvider :
    RunningRecordsComponentProvider,
    RecordsComponentProvider