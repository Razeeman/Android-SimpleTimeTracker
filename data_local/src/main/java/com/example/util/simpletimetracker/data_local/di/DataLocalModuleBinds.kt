package com.example.util.simpletimetracker.data_local.di

import com.example.util.simpletimetracker.data_local.activityFilter.ActivityFilterRepoImpl
import com.example.util.simpletimetracker.data_local.activitySuggestion.ActivitySuggestionRepoImpl
import com.example.util.simpletimetracker.data_local.category.CategoryRepoImpl
import com.example.util.simpletimetracker.data_local.category.RecordTypeCategoryRepoImpl
import com.example.util.simpletimetracker.data_local.complexRule.ComplexRuleRepoImpl
import com.example.util.simpletimetracker.data_local.favourite.FavouriteColorRepoImpl
import com.example.util.simpletimetracker.data_local.favourite.FavouriteCommentRepoImpl
import com.example.util.simpletimetracker.data_local.favourite.FavouriteIconRepoImpl
import com.example.util.simpletimetracker.data_local.favourite.RecordTypeToFavouriteCommentRepoImpl
import com.example.util.simpletimetracker.data_local.prefs.PrefsRepoImpl
import com.example.util.simpletimetracker.data_local.record.RecordRepoImpl
import com.example.util.simpletimetracker.data_local.record.RunningRecordRepoImpl
import com.example.util.simpletimetracker.data_local.recordTag.RunningRecordToRecordTagRepoImpl
import com.example.util.simpletimetracker.data_local.recordTag.RecordTagRepoImpl
import com.example.util.simpletimetracker.data_local.recordTag.RecordToRecordTagRepoImpl
import com.example.util.simpletimetracker.data_local.recordTag.RecordTypeToDefaultTagRepoImpl
import com.example.util.simpletimetracker.data_local.recordTag.RecordTypeToTagRepoImpl
import com.example.util.simpletimetracker.data_local.recordType.RecordTypeGoalRepoImpl
import com.example.util.simpletimetracker.data_local.recordType.RecordTypeRepoImpl
import com.example.util.simpletimetracker.data_local.backup.BackupPartialRepoImpl
import com.example.util.simpletimetracker.data_local.backup.BackupRepoImpl
import com.example.util.simpletimetracker.data_local.durationSuggestion.DurationSuggestionRepoImpl
import com.example.util.simpletimetracker.data_local.file.CsvRepoImpl
import com.example.util.simpletimetracker.data_local.file.IcsRepoImpl
import com.example.util.simpletimetracker.data_local.recordShortcut.RecordShortcutRepoImpl
import com.example.util.simpletimetracker.data_local.recordTag.RecordShortcutToRecordTagRepoImpl
import com.example.util.simpletimetracker.data_local.recordsFilter.FavouriteRecordsFilterRepoImpl
import com.example.util.simpletimetracker.data_local.sharing.SharingRepoImpl
import com.example.util.simpletimetracker.domain.activityFilter.repo.ActivityFilterRepo
import com.example.util.simpletimetracker.domain.activitySuggestion.repo.ActivitySuggestionRepo
import com.example.util.simpletimetracker.domain.category.repo.CategoryRepo
import com.example.util.simpletimetracker.domain.complexRule.repo.ComplexRuleRepo
import com.example.util.simpletimetracker.domain.favourite.repo.FavouriteColorRepo
import com.example.util.simpletimetracker.domain.favourite.repo.FavouriteCommentRepo
import com.example.util.simpletimetracker.domain.favourite.repo.FavouriteIconRepo
import com.example.util.simpletimetracker.domain.favourite.repo.RecordTypeToFavouriteCommentRepo
import com.example.util.simpletimetracker.domain.prefs.repo.PrefsRepo
import com.example.util.simpletimetracker.domain.record.repo.RecordRepo
import com.example.util.simpletimetracker.domain.recordTag.repo.RecordTagRepo
import com.example.util.simpletimetracker.domain.recordTag.repo.RecordToRecordTagRepo
import com.example.util.simpletimetracker.domain.category.repo.RecordTypeCategoryRepo
import com.example.util.simpletimetracker.domain.recordType.repo.RecordTypeGoalRepo
import com.example.util.simpletimetracker.domain.recordType.repo.RecordTypeRepo
import com.example.util.simpletimetracker.domain.recordTag.repo.RecordTypeToDefaultTagRepo
import com.example.util.simpletimetracker.domain.recordTag.repo.RecordTypeToTagRepo
import com.example.util.simpletimetracker.domain.record.repo.RunningRecordRepo
import com.example.util.simpletimetracker.domain.recordTag.repo.RunningRecordToRecordTagRepo
import com.example.util.simpletimetracker.domain.backup.repo.BackupPartialRepo
import com.example.util.simpletimetracker.domain.backup.repo.BackupRepo
import com.example.util.simpletimetracker.domain.backup.repo.CsvRepo
import com.example.util.simpletimetracker.domain.backup.repo.IcsRepo
import com.example.util.simpletimetracker.domain.durationSuggestion.repo.DurationSuggestionRepo
import com.example.util.simpletimetracker.domain.recordShortcut.repo.RecordShortcutRepo
import com.example.util.simpletimetracker.domain.recordTag.repo.RecordShortcutToRecordTagRepo
import com.example.util.simpletimetracker.domain.recordsFilter.repo.FavouriteRecordsFilterRepo
import com.example.util.simpletimetracker.domain.sharing.SharingRepo
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
    fun bindBackupPartialRepo(impl: BackupPartialRepoImpl): BackupPartialRepo

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
    fun bindRecordShortcutToRecordTagRepo(impl: RecordShortcutToRecordTagRepoImpl): RecordShortcutToRecordTagRepo

    @Binds
    @Singleton
    fun bindActivityFilterRepo(impl: ActivityFilterRepoImpl): ActivityFilterRepo

    @Binds
    @Singleton
    fun bindActivitySuggestionRepo(impl: ActivitySuggestionRepoImpl): ActivitySuggestionRepo

    @Binds
    @Singleton
    fun bindFavouriteCommentRepo(impl: FavouriteCommentRepoImpl): FavouriteCommentRepo

    @Binds
    @Singleton
    fun bindRecordTagToFavouriteCommentRepo(impl: RecordTypeToFavouriteCommentRepoImpl): RecordTypeToFavouriteCommentRepo

    @Binds
    @Singleton
    fun bindFavouriteColorRepo(impl: FavouriteColorRepoImpl): FavouriteColorRepo

    @Binds
    @Singleton
    fun bindRecordTypeGoalRepo(impl: RecordTypeGoalRepoImpl): RecordTypeGoalRepo

    @Binds
    @Singleton
    fun bindFavouriteIconRepo(impl: FavouriteIconRepoImpl): FavouriteIconRepo

    @Binds
    @Singleton
    fun bindComplexRuleRepo(impl: ComplexRuleRepoImpl): ComplexRuleRepo

    @Binds
    @Singleton
    fun bindDurationSuggestionRepo(impl: DurationSuggestionRepoImpl): DurationSuggestionRepo

    @Binds
    @Singleton
    fun bindRecordShortcutRepo(impl: RecordShortcutRepoImpl): RecordShortcutRepo

    @Binds
    @Singleton
    fun bindFavouriteRecordsFilterRepo(impl: FavouriteRecordsFilterRepoImpl): FavouriteRecordsFilterRepo
}