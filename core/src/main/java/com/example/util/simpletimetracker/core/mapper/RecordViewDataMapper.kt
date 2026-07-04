package com.example.util.simpletimetracker.core.mapper

import android.text.SpannableStringBuilder
import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.base.DurationFormat
import com.example.util.simpletimetracker.domain.record.mapper.DurationMapper
import com.example.util.simpletimetracker.domain.record.model.Record
import com.example.util.simpletimetracker.domain.record.model.RecordBase
import com.example.util.simpletimetracker.domain.recordTag.model.RecordTag
import com.example.util.simpletimetracker.domain.recordType.model.RecordType
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.empty.EmptyViewData
import com.example.util.simpletimetracker.feature_base_adapter.hintBig.HintBigViewData
import com.example.util.simpletimetracker.feature_base_adapter.record.RecordViewData
import com.example.util.simpletimetracker.feature_views.extension.setForegroundSpan
import com.example.util.simpletimetracker.feature_views.extension.setImageSpan
import com.example.util.simpletimetracker.feature_views.extension.toSpannableString
import com.example.util.simpletimetracker.feature_views.viewData.RecordTypeIcon
import javax.inject.Inject

class RecordViewDataMapper @Inject constructor(
    private val timeMapper: TimeMapper,
    private val iconMapper: IconMapper,
    private val colorMapper: ColorMapper,
    private val resourceRepo: ResourceRepo,
    private val durationMapper: DurationMapper,
    private val recordTagFullNameMapper: RecordTagFullNameMapper,
) {

    fun map(
        record: Record,
        recordType: RecordType?,
        recordTags: List<RecordTag>,
        isDarkTheme: Boolean,
        useMilitaryTime: Boolean,
        durationFormat: DurationFormat,
        showSeconds: Boolean,
    ): RecordViewData.Tracked {
        val tagIds = record.tags.map(RecordBase.Tag::tagId)

        return RecordViewData.Tracked(
            id = record.id,
            timeStartedTimestamp = record.timeStarted,
            timeEndedTimestamp = record.timeEnded,
            name = recordType?.name
                ?: resourceRepo.getString(R.string.untracked_time_name),
            tagName = recordTagFullNameMapper.getFullName(
                tags = recordTags.filter { it.id in tagIds },
                tagData = record.tags,
            ),
            timeStarted = timeMapper.formatTime(
                time = record.timeStarted,
                useMilitaryTime = useMilitaryTime,
                showSeconds = showSeconds,
            ),
            timeFinished = timeMapper.formatTime(
                time = record.timeEnded,
                useMilitaryTime = useMilitaryTime,
                showSeconds = showSeconds,
            ),
            duration = timeMapper.formatInterval(
                interval = durationMapper.map(
                    timeStarted = record.timeStarted,
                    timeEnded = record.timeEnded,
                    showSeconds = showSeconds,
                ),
                forceSeconds = showSeconds,
                durationFormat = durationFormat,
            ),
            iconId = recordType?.icon?.let {
                iconMapper.mapIcon(it)
            } ?: RecordTypeIcon.Image(R.drawable.unknown),
            color = recordType?.color?.let {
                colorMapper.mapToColorInt(
                    color = it,
                    isDarkTheme = isDarkTheme,
                )
            } ?: colorMapper.toUntrackedColor(isDarkTheme),
            comment = record.comment,
        )
    }

    fun map(
        record: Record,
        recordTypes: Map<Long, RecordType>,
        recordTags: List<RecordTag>,
        isDarkTheme: Boolean,
        useMilitaryTime: Boolean,
        durationFormat: DurationFormat,
        showSeconds: Boolean,
    ): RecordViewData.Tracked? {
        return map(
            record = record,
            recordType = recordTypes[record.typeId] ?: return null,
            recordTags = recordTags,
            isDarkTheme = isDarkTheme,
            useMilitaryTime = useMilitaryTime,
            durationFormat = durationFormat,
            showSeconds = showSeconds,
        )
    }

    fun mapToUntracked(
        timeStarted: Long,
        timeEnded: Long,
        isDarkTheme: Boolean,
        useMilitaryTime: Boolean,
        durationFormat: DurationFormat,
        showSeconds: Boolean,
    ): RecordViewData.Untracked {
        return RecordViewData.Untracked(
            name = resourceRepo.getString(R.string.untracked_time_name),
            timeStarted = timeMapper.formatTime(
                time = timeStarted,
                useMilitaryTime = useMilitaryTime,
                showSeconds = showSeconds,
            ),
            timeStartedTimestamp = timeStarted,
            timeFinished = timeMapper.formatTime(
                time = timeEnded,
                useMilitaryTime = useMilitaryTime,
                showSeconds = showSeconds,
            ),
            timeEndedTimestamp = timeEnded,
            duration = timeMapper.formatInterval(
                interval = durationMapper.map(
                    timeStarted = timeStarted,
                    timeEnded = timeEnded,
                    showSeconds = showSeconds,
                ),
                forceSeconds = showSeconds,
                durationFormat = durationFormat,
            ),
            iconId = RecordTypeIcon.Image(R.drawable.unknown),
            color = colorMapper.toUntrackedColor(isDarkTheme),
        )
    }

    fun mapFiltered(
        viewData: RecordViewData,
        isDarkTheme: Boolean,
        isFiltered: Boolean,
    ): RecordViewData {
        return when {
            isFiltered && viewData is RecordViewData.Tracked -> {
                viewData.copy(color = colorMapper.toFilteredColor(isDarkTheme))
            }
            isFiltered && viewData is RecordViewData.Untracked -> {
                viewData.copy(color = colorMapper.toFilteredColor(isDarkTheme))
            }
            else -> viewData
        }
    }

    fun mapToEmpty(): ViewHolderType {
        return EmptyViewData(
            message = R.string.no_data.let(resourceRepo::getString),
        )
    }

    fun mapToNoRecords(): ViewHolderType {
        val imageTag = resourceRepo.getString(R.string.image_tag)
        val emptyHint = resourceRepo.getString(R.string.no_records_exist)
        val addHint = resourceRepo.getString(R.string.record_add_hint, imageTag)
            .toSpannableString()
            .apply {
                val icon = resourceRepo.getDrawable(R.drawable.add)
                    ?.mutate()
                    ?.apply { setTint(resourceRepo.getColor(R.color.textHintCommon)) }
                    ?: return@apply
                setImageSpan(
                    start = indexOf(imageTag),
                    length = imageTag.length,
                    drawable = icon,
                    sizeDp = 16,
                    isCentered = true,
                )
                setForegroundSpan(color = resourceRepo.getColor(R.color.textHintCommon))
            }
        val dateHint = resourceRepo.getString(R.string.record_date_hint)
            .toSpannableString()
            .apply {
                setForegroundSpan(color = resourceRepo.getColor(R.color.textHintCommon))
            }

        return HintBigViewData(
            text = SpannableStringBuilder()
                .append(emptyHint)
                .append("\n")
                .append(addHint)
                .append("\n")
                .append(dateHint),
            infoIconVisible = true,
            closeIconVisible = false,
        )
    }
}