package com.example.util.simpletimetracker.feature_widget.universal.mapper

import android.graphics.Color
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.IconMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.domain.model.RunningRecord
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.hint.HintViewData
import com.example.util.simpletimetracker.feature_views.viewData.RecordTypeIcon
import com.example.util.simpletimetracker.feature_widget.R
import com.example.util.simpletimetracker.feature_widget.universal.customView.IconStackData
import com.example.util.simpletimetracker.feature_widget.universal.customView.WidgetUniversalViewData
import javax.inject.Inject

class WidgetUniversalViewDataMapper @Inject constructor(
    private val colorMapper: ColorMapper,
    private val iconMapper: IconMapper,
    private val resourceRepo: ResourceRepo,
) {

    fun mapToWidgetViewData(
        runningRecords: List<RunningRecord>,
        recordTypes: Map<Long, RecordType>,
        isDarkTheme: Boolean,
        backgroundTransparency: Long,
    ): WidgetUniversalViewData {
        val data = runningRecords.map { runningRecord ->
            val recordType = recordTypes[runningRecord.id]

            val icon = recordType?.icon
                ?.let(iconMapper::mapIcon)
                ?: RecordTypeIcon.Image(R.drawable.unknown)
            val color = recordType?.color
                ?.let { colorMapper.mapToColorInt(it, isDarkTheme) }
                ?: Color.BLACK

            IconStackData(icon = icon, iconBackgroundColor = color)
        }

        return WidgetUniversalViewData(
            data = data,
            iconColor = R.color.white.let(resourceRepo::getColor),
            backgroundAlpha = 1f - backgroundTransparency / 100f,
        )
    }

    fun mapToEmptyWidgetViewData(
        backgroundTransparency: Long,
    ): WidgetUniversalViewData {
        val icon = RecordTypeIcon.Image(R.drawable.ic_alarm_on_24px)
        val color = R.color.transparent.let(resourceRepo::getColor)
        val data = IconStackData(icon, color)

        return WidgetUniversalViewData(
            data = listOf(data),
            iconColor = R.color.widget_universal_empty_color.let(resourceRepo::getColor),
            backgroundAlpha = 1f - backgroundTransparency / 100f,
        )
    }

    fun mapToHint(): ViewHolderType {
        return HintViewData(
            text = R.string.running_records_empty.let(resourceRepo::getString),
        )
    }
}