package com.example.util.simpletimetracker.data_local.di

import com.example.util.simpletimetracker.data_local.repo.CategoryRepoImpl
import com.example.util.simpletimetracker.data_local.repo.PrefsRepoImpl
import com.example.util.simpletimetracker.data_local.repo.RecordCacheRepoImpl
import com.example.util.simpletimetracker.data_local.repo.RecordRepoImpl
import com.example.util.simpletimetracker.data_local.repo.RecordTagRepoImpl
import com.example.util.simpletimetracker.data_local.repo.RecordToRecordTagRepoImpl
import com.example.util.simpletimetracker.data_local.repo.RecordTypeCacheRepoImpl
import com.example.util.simpletimetracker.data_local.repo.RecordTypeCategoryRepoImpl
import com.example.util.simpletimetracker.data_local.repo.RecordTypeRepoImpl
import com.example.util.simpletimetracker.data_local.repo.RunningRecordRepoImpl
import com.example.util.simpletimetracker.data_local.resolver.BackupRepoImpl
import com.example.util.simpletimetracker.data_local.resolver.CsvRepoImpl
import com.example.util.simpletimetracker.domain.repo.CategoryRepo
import com.example.util.simpletimetracker.domain.repo.PrefsRepo
import com.example.util.simpletimetracker.domain.repo.RecordCacheRepo
import com.example.util.simpletimetracker.domain.repo.RecordRepo
import com.example.util.simpletimetracker.domain.repo.RecordTagRepo
import com.example.util.simpletimetracker.domain.repo.RecordToRecordTagRepo
import com.example.util.simpletimetracker.domain.repo.RecordTypeCacheRepo
import com.example.util.simpletimetracker.domain.repo.RecordTypeCategoryRepo
import com.example.util.simpletimetracker.domain.repo.RecordTypeRepo
import com.example.util.simpletimetracker.domain.repo.RunningRecordRepo
import com.example.util.simpletimetracker.domain.resolver.BackupRepo
import com.example.util.simpletimetracker.domain.resolver.CsvRepo
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataLocalModuleBinds {

    @Binds
    @Singleton
    abstract fun getRecordRepo(impl: RecordRepoImpl): RecordRepo

    @Binds
    @Singleton
    abstract fun getRecordCacheRepo(impl: RecordCacheRepoImpl): RecordCacheRepo

    @Binds
    @Singleton
    abstract fun getRecordTypeRepo(impl: RecordTypeRepoImpl): RecordTypeRepo

    @Binds
    @Singleton
    abstract fun getRecordTypeCacheRepo(impl: RecordTypeCacheRepoImpl): RecordTypeCacheRepo

    @Binds
    @Singleton
    abstract fun getRunningRecordRepo(impl: RunningRecordRepoImpl): RunningRecordRepo

    @Binds
    @Singleton
    abstract fun getPrefsRepo(impl: PrefsRepoImpl): PrefsRepo

    @Binds
    @Singleton
    abstract fun getBackupRepo(impl: BackupRepoImpl): BackupRepo

    @Binds
    @Singleton
    abstract fun getCsvRepo(impl: CsvRepoImpl): CsvRepo

    @Binds
    @Singleton
    abstract fun getCategoryRepo(impl: CategoryRepoImpl): CategoryRepo

    @Binds
    @Singleton
    abstract fun getRecordTagRepo(impl: RecordTagRepoImpl): RecordTagRepo

    @Binds
    @Singleton
    abstract fun getRecordTypeCategoryRepo(impl: RecordTypeCategoryRepoImpl): RecordTypeCategoryRepo

    @Binds
    @Singleton
    abstract fun getRecordToRecordTagRepo(impl: RecordToRecordTagRepoImpl): RecordToRecordTagRepo
}