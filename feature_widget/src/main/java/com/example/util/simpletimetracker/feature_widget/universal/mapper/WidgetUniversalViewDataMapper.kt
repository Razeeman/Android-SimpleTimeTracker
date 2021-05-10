package com.example.util.simpletimetracker.feature_widget.universal.mapper

import android.graphics.Color
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.IconMapper
import com.example.util.simpletimetracker.core.mapper.RecordTypeViewDataMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.core.viewData.RecordTypeIcon
import com.example.util.simpletimetracker.core.viewData.RecordTypeViewData
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.domain.model.RunningRecord
import com.example.util.simpletimetracker.feature_widget.R
import com.example.util.simpletimetracker.feature_widget.universal.customView.IconStackData
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
        numberOfCards: Int,
        isDarkTheme: Boolean
    ): RecordTypeViewData {
        return recordTypeViewDataMapper.mapFiltered(
            recordType,
            numberOfCards,
            isDarkTheme,
            isFiltered
        )
    }

    fun mapToWidgetViewData(
        runningRecords: List<RunningRecord>,
        recordTypes: Map<Long, RecordType>,
        isDarkTheme: Boolean
    ): WidgetUniversalViewData {
        val data = runningRecords.map { runningRecord ->
            val recordType = recordTypes[runningRecord.id]

            val icon = recordType?.icon
                ?.let(iconMapper::mapIcon)
                ?: RecordTypeIcon.Image(R.drawable.unknown)
            val color = recordType?.color
                ?.let { colorMapper.mapToColorResId(it, isDarkTheme) }
                ?.let(resourceRepo::getColor)
                ?: Color.BLACK

            IconStackData(icon = icon, iconBackgroundColor = color)
        }

        return WidgetUniversalViewData(
            data = data,
            iconColor = R.color.white.let(resourceRepo::getColor)
        )
    }

    fun mapToEmptyWidgetViewData(): WidgetUniversalViewData {
        val icon = RecordTypeIcon.Image(R.drawable.ic_alarm_on_24px)
        val color = R.color.transparent.let(resourceRepo::getColor)
        val data = IconStackData(icon, color)

        return WidgetUniversalViewData(
            data = listOf(data),
            iconColor = R.color.widget_universal_empty_color.let(resourceRepo::getColor)
        )
    }
}