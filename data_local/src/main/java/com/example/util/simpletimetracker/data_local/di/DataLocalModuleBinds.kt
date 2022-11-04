package com.example.util.simpletimetracker.data_local.di

import com.example.util.simpletimetracker.data_local.repo.ActivityFilterRepoImpl
import com.example.util.simpletimetracker.data_local.repo.CategoryRepoImpl
import com.example.util.simpletimetracker.data_local.repo.PrefsRepoImpl
import com.example.util.simpletimetracker.data_local.repo.RecordRepoImpl
import com.example.util.simpletimetracker.data_local.repo.RecordTagRepoImpl
import com.example.util.simpletimetracker.data_local.repo.RecordToRecordTagRepoImpl
import com.example.util.simpletimetracker.data_local.repo.RecordTypeCategoryRepoImpl
import com.example.util.simpletimetracker.data_local.repo.RecordTypeRepoImpl
import com.example.util.simpletimetracker.data_local.repo.RunningRecordRepoImpl
import com.example.util.simpletimetracker.data_local.repo.RunningRecordToRecordTagRepoImpl
import com.example.util.simpletimetracker.data_local.resolver.BackupRepoImpl
import com.example.util.simpletimetracker.data_local.resolver.CsvRepoImpl
import com.example.util.simpletimetracker.domain.repo.ActivityFilterRepo
import com.example.util.simpletimetracker.domain.repo.CategoryRepo
import com.example.util.simpletimetracker.domain.repo.PrefsRepo
import com.example.util.simpletimetracker.domain.repo.RecordRepo
import com.example.util.simpletimetracker.domain.repo.RecordTagRepo
import com.example.util.simpletimetracker.domain.repo.RecordToRecordTagRepo
import com.example.util.simpletimetracker.domain.repo.RecordTypeCategoryRepo
import com.example.util.simpletimetracker.domain.repo.RecordTypeRepo
import com.example.util.simpletimetracker.domain.repo.RunningRecordRepo
import com.example.util.simpletimetracker.domain.repo.RunningRecordToRecordTagRepo
import com.example.util.simpletimetracker.domain.resolver.BackupRepo
import com.example.util.simpletimetracker.domain.resolver.CsvRepo
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface DataLocalModuleBinds {

    @Binds
    @Singleton
    fun RecordRepoImpl.bindRecordRepo(): RecordRepo

    @Binds
    @Singleton
    fun RecordTypeRepoImpl.bindRecordTypeRepo(): RecordTypeRepo

    @Binds
    @Singleton
    fun RunningRecordRepoImpl.bindRunningRecordRepo(): RunningRecordRepo

    @Binds
    @Singleton
    fun PrefsRepoImpl.bindPrefsRepo(): PrefsRepo

    @Binds
    @Singleton
    fun BackupRepoImpl.bindBackupRepo(): BackupRepo

    @Binds
    @Singleton
    fun CsvRepoImpl.bindCsvRepo(): CsvRepo

    @Binds
    @Singleton
    fun CategoryRepoImpl.bindCategoryRepo(): CategoryRepo

    @Binds
    @Singleton
    fun RecordTagRepoImpl.bindRecordTagRepo(): RecordTagRepo

    @Binds
    @Singleton
    fun RecordTypeCategoryRepoImpl.bindRecordTypeCategoryRepo(): RecordTypeCategoryRepo

    @Binds
    @Singleton
    fun RecordToRecordTagRepoImpl.bindRecordToRecordTagRepo(): RecordToRecordTagRepo

    @Binds
    @Singleton
    fun RunningRecordToRecordTagRepoImpl.bindRunningRecordToRecordTagRepo(): RunningRecordToRecordTagRepo

    @Binds
    @Singleton
    fun ActivityFilterRepoImpl.bindActivityFilterRepo(): ActivityFilterRepo
}