package com.example.util.simpletimetracker.core.interactor

import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.interactor.AddRunningRecordMediator
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.domain.model.RepeatButtonType
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.notification.SnackBarParams
import javax.inject.Inject

class RecordRepeatInteractor @Inject constructor(
    private val recordInteractor: RecordInteractor,
    private val runningRecordInteractor: RunningRecordInteractor,
    private val addRunningRecordMediator: AddRunningRecordMediator,
    private val prefsInteractor: PrefsInteractor,
    private val router: Router,
    private val resourceRepo: ResourceRepo,
) {

    suspend fun shouldShowButton(): Boolean {
        return !recordInteractor.isEmpty()
    }

    suspend fun execute() {
        val type = prefsInteractor.getRepeatButtonType()

        val prevRecord = recordInteractor.getPrev(
            timeStarted = System.currentTimeMillis(),
            limit = 2,
        ).let {
            when (type) {
                is RepeatButtonType.RepeatLast -> it.getOrNull(0)
                is RepeatButtonType.RepeatBeforeLast -> it.getOrNull(1)
            }
        } ?: run {
            SnackBarParams(
                message = resourceRepo.getString(R.string.running_records_repeat_no_prev_record),
                duration = SnackBarParams.Duration.Short,
            ).let(router::show)
            return
        }
        if (runningRecordInteractor.get(prevRecord.typeId) != null) {
            SnackBarParams(
                message = resourceRepo.getString(R.string.running_records_repeat_already_tracking),
                duration = SnackBarParams.Duration.Short,
            ).let(router::show)
            return
        }

        addRunningRecordMediator.startTimer(
            typeId = prevRecord.typeId,
            tagIds = prevRecord.tagIds,
            comment = prevRecord.comment,
        )
    }
}