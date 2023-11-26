package com.example.util.simpletimetracker.feature_running_records.mapper

import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.empty.EmptyViewData
import com.example.util.simpletimetracker.feature_base_adapter.hint.HintViewData
import com.example.util.simpletimetracker.feature_base_adapter.hintBig.HintBigViewData
import com.example.util.simpletimetracker.feature_running_records.R
import javax.inject.Inject

class RunningRecordsViewDataMapper @Inject constructor(
    private val resourceRepo: ResourceRepo,
) {

    fun mapToTypesEmpty(): ViewHolderType {
        return HintBigViewData(
            text = resourceRepo.getString(
                R.string.running_records_types_empty,
                resourceRepo.getString(R.string.running_records_add_type),
                resourceRepo.getString(R.string.running_records_add_default),
            ),
            infoIconVisible = true,
            closeIconVisible = false,
        )
    }

    fun mapToEmpty(): ViewHolderType {
        return EmptyViewData(
            message = R.string.running_records_empty.let(resourceRepo::getString),
            hint = R.string.running_records_empty_hint.let(resourceRepo::getString),
        )
    }

    fun mapToHasRunningRecords(): ViewHolderType {
        return HintViewData(
            text = R.string.running_records_has_timers.let(resourceRepo::getString),
            paddingVertical = 0,
        )
    }
}