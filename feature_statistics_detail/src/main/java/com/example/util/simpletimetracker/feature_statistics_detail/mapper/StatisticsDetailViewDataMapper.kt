package com.example.util.simpletimetracker.feature_statistics_detail.mapper

import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.IconMapper
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.domain.model.StatisticsDetail
import com.example.util.simpletimetracker.feature_statistics_detail.R
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailViewData
import javax.inject.Inject

class StatisticsDetailViewDataMapper @Inject constructor(
    private val iconMapper: IconMapper,
    private val colorMapper: ColorMapper,
    private val timeMapper: TimeMapper,
    private val resourceRepo: ResourceRepo
) {

    fun map(
        recordType: RecordType?,
        statisticsDetail: StatisticsDetail
    ): StatisticsDetailViewData {
        return StatisticsDetailViewData(
            name = recordType?.name.orEmpty(),
            iconId = recordType?.icon
                ?.let(iconMapper::mapToDrawableResId)
                ?: R.drawable.unknown,
            color = (recordType?.color
                ?.let(colorMapper::mapToColorResId)
                ?: R.color.untracked_time_color)
                .let(resourceRepo::getColor),
            totalDuration = statisticsDetail.totalDuration
                .let(timeMapper::formatInterval),
            timesTracked = statisticsDetail.timesTracked.toString()
        )
    }

    fun mapToUntracked(): StatisticsDetailViewData {
        return StatisticsDetailViewData(
            name = resourceRepo.getString(R.string.untracked_time_name),
            iconId = R.drawable.unknown,
            color = R.color.untracked_time_color.let(resourceRepo::getColor),
            totalDuration = "",
            timesTracked = "0"
        )
    }
}