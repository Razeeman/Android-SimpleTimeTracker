package com.example.util.simpletimetracker.domain.backup.interactor

import com.example.util.simpletimetracker.domain.activityFilter.repo.ActivityFilterRepo
import com.example.util.simpletimetracker.domain.activitySuggestion.repo.ActivitySuggestionRepo
import com.example.util.simpletimetracker.domain.category.repo.CategoryRepo
import com.example.util.simpletimetracker.domain.category.repo.RecordTypeCategoryRepo
import com.example.util.simpletimetracker.domain.complexRule.repo.ComplexRuleRepo
import com.example.util.simpletimetracker.domain.durationSuggestion.repo.DurationSuggestionRepo
import com.example.util.simpletimetracker.domain.favourite.repo.FavouriteColorRepo
import com.example.util.simpletimetracker.domain.favourite.repo.FavouriteCommentRepo
import com.example.util.simpletimetracker.domain.favourite.repo.FavouriteIconRepo
import com.example.util.simpletimetracker.domain.record.repo.RecordRepo
import com.example.util.simpletimetracker.domain.record.repo.RunningRecordRepo
import com.example.util.simpletimetracker.domain.recordShortcut.repo.RecordShortcutRepo
import com.example.util.simpletimetracker.domain.recordTag.repo.RecordShortcutToRecordTagRepo
import com.example.util.simpletimetracker.domain.recordTag.repo.RecordTagRepo
import com.example.util.simpletimetracker.domain.recordTag.repo.RecordToRecordTagRepo
import com.example.util.simpletimetracker.domain.recordTag.repo.RecordTypeToDefaultTagRepo
import com.example.util.simpletimetracker.domain.recordTag.repo.RecordTypeToTagRepo
import com.example.util.simpletimetracker.domain.recordTag.repo.RunningRecordToRecordTagRepo
import com.example.util.simpletimetracker.domain.recordType.repo.RecordTypeGoalRepo
import com.example.util.simpletimetracker.domain.recordType.repo.RecordTypeRepo
import com.example.util.simpletimetracker.domain.recordsFilter.repo.FavouriteRecordsFilterRepo
import javax.inject.Inject

class ClearDataInteractor @Inject constructor(
    private val recordTypeRepo: RecordTypeRepo,
    private val recordRepo: RecordRepo,
    private val recordShortcutRepo: RecordShortcutRepo,
    private val categoryRepo: CategoryRepo,
    private val recordTypeCategoryRepo: RecordTypeCategoryRepo,
    private val recordToRecordTagRepo: RecordToRecordTagRepo,
    private val recordTagRepo: RecordTagRepo,
    private val activityFilterRepo: ActivityFilterRepo,
    private val activitySuggestionRepo: ActivitySuggestionRepo,
    private val runningRecordRepo: RunningRecordRepo,
    private val runningRecordToRecordTagRepo: RunningRecordToRecordTagRepo,
    private val favouriteCommentRepo: FavouriteCommentRepo,
    private val favouriteColorRepo: FavouriteColorRepo,
    private val recordTypeGoalRepo: RecordTypeGoalRepo,
    private val recordTypeToTagRepo: RecordTypeToTagRepo,
    private val recordTypeToDefaultTagRepo: RecordTypeToDefaultTagRepo,
    private val favouriteIconRepo: FavouriteIconRepo,
    private val complexRuleRepo: ComplexRuleRepo,
    private val durationSuggestionRepo: DurationSuggestionRepo,
    private val favouriteRecordsFilterRepo: FavouriteRecordsFilterRepo,
    private val recordShortcutToRecordTagRepo: RecordShortcutToRecordTagRepo,
) {

    suspend fun execute() {
        recordTypeRepo.clear()
        recordRepo.clear()
        recordShortcutRepo.clear()
        categoryRepo.clear()
        recordTypeCategoryRepo.clear()
        recordTagRepo.clear()
        recordToRecordTagRepo.clear()
        activityFilterRepo.clear()
        activitySuggestionRepo.clear()
        runningRecordRepo.clear()
        runningRecordToRecordTagRepo.clear()
        favouriteCommentRepo.clear()
        favouriteColorRepo.clear()
        recordTypeGoalRepo.clear()
        recordTypeToTagRepo.clear()
        recordTypeToDefaultTagRepo.clear()
        favouriteIconRepo.clear()
        complexRuleRepo.clear()
        durationSuggestionRepo.clear()
        favouriteRecordsFilterRepo.clear()
        recordShortcutToRecordTagRepo.clear()
    }
}