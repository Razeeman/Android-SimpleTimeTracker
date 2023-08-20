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
import com.example.util.simpletimetracker.navigation.params.notification.ToastParams
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

    suspend fun repeat(): Boolean {
        return execute { messageResId ->
            SnackBarParams(
                message = resourceRepo.getString(messageResId),
                duration = SnackBarParams.Duration.Short,
            ).let(router::show)
        }
    }

    // Can be used than app is closed (ex. from widget).
    suspend fun repeatExternal() {
        execute { messageResId ->
            ToastParams(
                message = resourceRepo.getString(messageResId),
            ).let(router::show)
        }
    }

    private suspend fun execute(
        messageShower: (messageResId: Int) -> Unit,
    ): Boolean {
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
            messageShower(R.string.running_records_repeat_no_prev_record)
            return false
        }
        if (runningRecordInteractor.get(prevRecord.typeId) != null) {
            messageShower(R.string.running_records_repeat_already_tracking)
            return false
        }

        addRunningRecordMediator.startTimer(
            typeId = prevRecord.typeId,
            tagIds = prevRecord.tagIds,
            comment = prevRecord.comment,
        )
        return true
    }
}