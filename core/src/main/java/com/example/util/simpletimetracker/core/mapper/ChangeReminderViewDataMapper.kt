package com.example.util.simpletimetracker.core.mapper

import android.text.SpannableString
import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.utils.LocalDateMapper
import com.example.util.simpletimetracker.feature_views.extension.setImageSpan
import com.example.util.simpletimetracker.feature_views.extension.toSpannableString
import java.time.LocalDate
import java.util.TimeZone
import javax.inject.Inject

class ChangeReminderViewDataMapper @Inject constructor(
    private val timeMapper: TimeMapper,
    private val resourceRepo: ResourceRepo,
    private val localDateMapper: LocalDateMapper,
) {

    fun mapDndHint(
        doNotDisturbStartMillis: Long,
        doNotDisturbEndMillis: Long,
        useMilitaryTime: Boolean,
        iconColor: Int,
    ): SpannableString? {
        if (doNotDisturbStartMillis == doNotDisturbEndMillis) return null

        fun formatTime(
            timeOfDayMillis: Long,
        ): String {
            val timeZone = TimeZone.getDefault()
            return formatTimeOfDay(
                millis = timeOfDayMillis,
                useMilitaryTime = useMilitaryTime,
                date = LocalDate.now(timeZone.toZoneId()),
                timeZone = timeZone,
            )
        }

        val hint = listOf(
            formatTime(doNotDisturbStartMillis),
            formatTime(doNotDisturbEndMillis),
        ).joinToString(separator = "-")

        val icon = resourceRepo.getDrawable(R.drawable.disabled)
            ?.mutate()
            ?.apply { setTint(iconColor) }

        return hint
            .let { IMAGE_TAG + it }
            .toSpannableString()
            .apply {
                setImageSpan(
                    start = indexOf(IMAGE_TAG),
                    length = IMAGE_TAG.length,
                    drawable = icon ?: return@apply,
                    sizeDp = 16,
                    isCentered = true,
                )
            }
    }

    fun formatTimeOfDay(
        millis: Long,
        useMilitaryTime: Boolean,
        date: LocalDate,
        timeZone: TimeZone,
    ): String {
        val timestamp = localDateMapper.resolveDateTime(
            date = date,
            timeOfDayMillis = millis,
            timeZone = timeZone,
        ) ?: return resourceRepo.getString(R.string.no_data)
        return timeMapper.formatTime(
            time = timestamp,
            useMilitaryTime = useMilitaryTime,
            showSeconds = false,
        )
    }

    companion object {
        private const val IMAGE_TAG = "[IMAGE_TAG]"
    }
}