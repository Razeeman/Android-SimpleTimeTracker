package com.example.util.simpletimetracker.feature_widget.universal.mapper

import android.graphics.Color
import com.example.util.simpletimetracker.core.adapter.ViewHolderType
import com.example.util.simpletimetracker.core.adapter.empty.EmptyViewData
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.IconMapper
import com.example.util.simpletimetracker.core.mapper.RecordTypeViewDataMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.core.viewData.RecordTypeViewData
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.domain.model.RunningRecord
import com.example.util.simpletimetracker.feature_widget.R
import com.example.util.simpletimetracker.feature_widget.universal.customView.WidgetUniversalViewData
import javax.inject.Inject

class WidgetUniversalViewDataMapper @Inject constructor(
    private val colorMapper: ColorMapper,
    private val iconMapper: IconMapper,
    private val resourceRepo: ResourceRepo,
    private val recordTypeViewDataMapper: RecordTypeViewDataMapper
) {

    fun map(
        recordType: RecordType,
        isFiltered: Boolean,
        numberOfCards: Int
    ): RecordTypeViewData {
        return recordTypeViewDataMapper.map(recordType, numberOfCards).copy(
            color = if (isFiltered) {
                R.color.filtered_color
            } else {
                recordType.color.let(colorMapper::mapToColorResId)
            }.let(resourceRepo::getColor)
        )
    }

    fun mapToEmpty(): ViewHolderType {
        return EmptyViewData(
            message = R.string.widget_empty.let(resourceRepo::getString)
        )
    }

    fun mapToWidgetViewData(
        runningRecords: List<RunningRecord>,
        recordTypes: Map<Long, RecordType>
    ): WidgetUniversalViewData {
        val data = runningRecords.map { runningRecord ->
            val recordType = recordTypes[runningRecord.id]

            val icon = recordType?.icon
                ?.let(iconMapper::mapToDrawableResId)
                ?: R.drawable.unknown
            val color = recordType?.color
                ?.let(colorMapper::mapToColorResId)
                ?.let(resourceRepo::getColor)
                ?: Color.BLACK

            icon to color
        }

        return WidgetUniversalViewData(
            data = data,
            iconColor = R.color.white.let(resourceRepo::getColor)
        )
    }

    fun mapToEmptyWidgetViewData(): WidgetUniversalViewData {
        val icon = R.drawable.ic_alarm_on_24px
        val color = R.color.transparent.let(resourceRepo::getColor)

        return WidgetUniversalViewData(
            data = listOf(icon to color),
            iconColor = R.color.widget_universal_empty_color.let(resourceRepo::getColor)
        )
    }
}