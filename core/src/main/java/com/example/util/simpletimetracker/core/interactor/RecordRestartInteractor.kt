package com.example.util.simpletimetracker.core.interactor

import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.interactor.AddRunningRecordMediator
import com.example.util.simpletimetracker.domain.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.notification.SnackBarParams
import javax.inject.Inject

class RecordRestartInteractor @Inject constructor(
    private val recordInteractor: RecordInteractor,
    private val runningRecordInteractor: RunningRecordInteractor,
    private val addRunningRecordMediator: AddRunningRecordMediator,
    private val router: Router,
    private val resourceRepo: ResourceRepo,
) {

    suspend fun execute() {
        val prevRecord = recordInteractor.getPrev(System.currentTimeMillis()) ?: run {
            SnackBarParams(
                message = resourceRepo.getString(R.string.running_records_restart_no_prev_record),
                duration = SnackBarParams.Duration.Short,
            ).let(router::show)
            return
        }
        if (runningRecordInteractor.get(prevRecord.typeId) != null) {
            SnackBarParams(
                message = resourceRepo.getString(R.string.running_records_restart_already_tracking),
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