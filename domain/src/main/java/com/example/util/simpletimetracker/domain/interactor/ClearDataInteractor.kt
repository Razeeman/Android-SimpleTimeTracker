package com.example.util.simpletimetracker.domain.interactor

import com.example.util.simpletimetracker.domain.repo.ActivityFilterRepo
import com.example.util.simpletimetracker.domain.repo.CategoryRepo
import com.example.util.simpletimetracker.domain.repo.ComplexRuleRepo
import com.example.util.simpletimetracker.domain.repo.FavouriteColorRepo
import com.example.util.simpletimetracker.domain.repo.FavouriteCommentRepo
import com.example.util.simpletimetracker.domain.repo.FavouriteIconRepo
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
import javax.inject.Inject

class ClearDataInteractor @Inject constructor(
    private val recordTypeRepo: RecordTypeRepo,
    private val recordRepo: RecordRepo,
    private val categoryRepo: CategoryRepo,
    private val recordTypeCategoryRepo: RecordTypeCategoryRepo,
    private val recordToRecordTagRepo: RecordToRecordTagRepo,
    private val recordTagRepo: RecordTagRepo,
    private val activityFilterRepo: ActivityFilterRepo,
    private val runningRecordRepo: RunningRecordRepo,
    private val runningRecordToRecordTagRepo: RunningRecordToRecordTagRepo,
    private val favouriteCommentRepo: FavouriteCommentRepo,
    private val favouriteColorRepo: FavouriteColorRepo,
    private val recordTypeGoalRepo: RecordTypeGoalRepo,
    private val recordTypeToTagRepo: RecordTypeToTagRepo,
    private val recordTypeToDefaultTagRepo: RecordTypeToDefaultTagRepo,
    private val favouriteIconRepo: FavouriteIconRepo,
    private val complexRuleRepo: ComplexRuleRepo,
) {

    suspend fun execute() {
        recordTypeRepo.clear()
        recordRepo.clear()
        categoryRepo.clear()
        recordTypeCategoryRepo.clear()
        recordTagRepo.clear()
        recordToRecordTagRepo.clear()
        activityFilterRepo.clear()
        runningRecordRepo.clear()
        runningRecordToRecordTagRepo.clear()
        favouriteCommentRepo.clear()
        favouriteColorRepo.clear()
        recordTypeGoalRepo.clear()
        recordTypeToTagRepo.clear()
        recordTypeToDefaultTagRepo.clear()
        favouriteIconRepo.clear()
        complexRuleRepo.clear()
    }
}