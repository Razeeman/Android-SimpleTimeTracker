package com.example.util.simpletimetracker.data_local.di

import com.example.util.simpletimetracker.core.repo.DataEditRepo
import com.example.util.simpletimetracker.core.repo.FileWorkRepo
import com.example.util.simpletimetracker.data_local.repo.ActivityFilterRepoImpl
import com.example.util.simpletimetracker.data_local.repo.CategoryRepoImpl
import com.example.util.simpletimetracker.data_local.repo.ComplexRuleRepoImpl
import com.example.util.simpletimetracker.data_local.repo.DataEditRepoImpl
import com.example.util.simpletimetracker.data_local.repo.FavouriteCommentRepoImpl
import com.example.util.simpletimetracker.data_local.repo.FavouriteIconRepoImpl
import com.example.util.simpletimetracker.data_local.repo.FileWorkRepoImpl
import com.example.util.simpletimetracker.data_local.repo.PrefsRepoImpl
import com.example.util.simpletimetracker.data_local.repo.RecordRepoImpl
import com.example.util.simpletimetracker.data_local.repo.RecordTagRepoImpl
import com.example.util.simpletimetracker.data_local.repo.RecordToRecordTagRepoImpl
import com.example.util.simpletimetracker.data_local.repo.RecordTypeCategoryRepoImpl
import com.example.util.simpletimetracker.data_local.repo.RecordTypeGoalRepoImpl
import com.example.util.simpletimetracker.data_local.repo.RecordTypeRepoImpl
import com.example.util.simpletimetracker.data_local.repo.RecordTypeToDefaultTagRepoImpl
import com.example.util.simpletimetracker.data_local.repo.RecordTypeToTagRepoImpl
import com.example.util.simpletimetracker.data_local.repo.RunningRecordRepoImpl
import com.example.util.simpletimetracker.data_local.repo.RunningRecordToRecordTagRepoImpl
import com.example.util.simpletimetracker.data_local.resolver.BackupRepoImpl
import com.example.util.simpletimetracker.data_local.resolver.CsvRepoImpl
import com.example.util.simpletimetracker.data_local.resolver.IcsRepoImpl
import com.example.util.simpletimetracker.data_local.resolver.SharingRepoImpl
import com.example.util.simpletimetracker.domain.repo.ActivityFilterRepo
import com.example.util.simpletimetracker.domain.repo.CategoryRepo
import com.example.util.simpletimetracker.domain.repo.ComplexRuleRepo
import com.example.util.simpletimetracker.domain.repo.FavouriteCommentRepo
import com.example.util.simpletimetracker.domain.repo.FavouriteIconRepo
import com.example.util.simpletimetracker.domain.repo.PrefsRepo
import com.example.util.simpletimetracker.domain.repo.RecordRepo
import com.example.util.simpletimetracker.domain.repo.RecordTagRepo
import com.example.util.simpletimetracker.domain.repo.RecordToRecordTagRepo
import com.example.util.simpletimetracker.domain.repo.RecordTypeCategoryRepo
import com.example.util.simpletimetracker.domain.repo.RecordTypeGoalRepo
import com.example.util.simpletimetracker.domain.repo.RecordTypeRepo
import com.example.util.simpletimetracker.domain.repo.RecordTypeToDefaultTagRepo
import com.example.util.simpletimetracker.domain.repo.RecordTypeToTagRepo
import com.example.util.simpletimetracker.domain.repo.RunningRecordRepo
import com.example.util.simpletimetracker.domain.repo.RunningRecordToRecordTagRepo
import com.example.util.simpletimetracker.domain.resolver.BackupRepo
import com.example.util.simpletimetracker.domain.resolver.CsvRepo
import com.example.util.simpletimetracker.domain.resolver.IcsRepo
import com.example.util.simpletimetracker.domain.resolver.SharingRepo
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
    fun bindRecordRepo(impl: RecordRepoImpl): RecordRepo

    @Binds
    @Singleton
    fun bindRecordTypeRepo(impl: RecordTypeRepoImpl): RecordTypeRepo

    @Binds
    @Singleton
    fun bindRunningRecordRepo(impl: RunningRecordRepoImpl): RunningRecordRepo

    @Binds
    @Singleton
    fun bindPrefsRepo(impl: PrefsRepoImpl): PrefsRepo

    @Binds
    @Singleton
    fun bindBackupRepo(impl: BackupRepoImpl): BackupRepo

    @Binds
    @Singleton
    fun bindCsvRepo(impl: CsvRepoImpl): CsvRepo

    @Binds
    @Singleton
    fun bindIcsRepo(impl: IcsRepoImpl): IcsRepo

    @Binds
    @Singleton
    fun bindSharingRepo(impl: SharingRepoImpl): SharingRepo

    @Binds
    @Singleton
    fun bindCategoryRepo(impl: CategoryRepoImpl): CategoryRepo

    @Binds
    @Singleton
    fun bindRecordTagRepo(impl: RecordTagRepoImpl): RecordTagRepo

    @Binds
    @Singleton
    fun bindRecordTypeCategoryRepo(impl: RecordTypeCategoryRepoImpl): RecordTypeCategoryRepo

    @Binds
    @Singleton
    fun bindRecordTypeToTagRepo(impl: RecordTypeToTagRepoImpl): RecordTypeToTagRepo

    @Binds
    @Singleton
    fun bindRecordTypeToDefaultTagRepo(impl: RecordTypeToDefaultTagRepoImpl): RecordTypeToDefaultTagRepo

    @Binds
    @Singleton
    fun bindRecordToRecordTagRepo(impl: RecordToRecordTagRepoImpl): RecordToRecordTagRepo

    @Binds
    @Singleton
    fun bindRunningRecordToRecordTagRepo(impl: RunningRecordToRecordTagRepoImpl): RunningRecordToRecordTagRepo

    @Binds
    @Singleton
    fun bindActivityFilterRepo(impl: ActivityFilterRepoImpl): ActivityFilterRepo

    @Binds
    @Singleton
    fun bindFavouriteCommentRepo(impl: FavouriteCommentRepoImpl): FavouriteCommentRepo

    @Binds
    @Singleton
    fun bindRecordTypeGoalRepo(impl: RecordTypeGoalRepoImpl): RecordTypeGoalRepo

    @Binds
    @Singleton
    fun bindDataEditRepo(impl: DataEditRepoImpl): DataEditRepo

    @Binds
    @Singleton
    fun bindFileWorkRepo(impl: FileWorkRepoImpl): FileWorkRepo

    @Binds
    @Singleton
    fun bindFavouriteIconRepo(impl: FavouriteIconRepoImpl): FavouriteIconRepo

    @Binds
    @Singleton
    fun bindComplexRuleRepo(impl: ComplexRuleRepoImpl): ComplexRuleRepo
}