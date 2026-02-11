package com.example.util.simpletimetracker.feature_notification.activitySwitch.interactor

import com.example.util.simpletimetracker.core.interactor.CompleteTypesStateInteractor
import com.example.util.simpletimetracker.core.interactor.GetCurrentRecordsDurationInteractor
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.IconMapper
import com.example.util.simpletimetracker.core.mapper.RecordTagValueMapper
import com.example.util.simpletimetracker.core.mapper.RecordTagViewDataMapper
import com.example.util.simpletimetracker.core.mapper.RecordTypeViewDataMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.base.REPEAT_BUTTON_ITEM_ID
import com.example.util.simpletimetracker.domain.record.model.RecordBase
import com.example.util.simpletimetracker.domain.recordTag.interactor.GetSelectableTagsInteractor
import com.example.util.simpletimetracker.domain.recordTag.interactor.RecordTagInteractor
import com.example.util.simpletimetracker.domain.recordTag.model.RecordTag
import com.example.util.simpletimetracker.domain.recordType.model.RecordType
import com.example.util.simpletimetracker.domain.recordType.model.RecordTypeGoal
import com.example.util.simpletimetracker.feature_notification.R
import com.example.util.simpletimetracker.feature_notification.activitySwitch.manager.NotificationControlsManager.Companion.APPLY_TAGS_ID
import com.example.util.simpletimetracker.feature_notification.activitySwitch.manager.NotificationControlsManager.Companion.TAGS_LIST_SIZE
import com.example.util.simpletimetracker.feature_notification.activitySwitch.manager.NotificationControlsManager.Companion.TYPES_LIST_SIZE
import com.example.util.simpletimetracker.feature_notification.activitySwitch.manager.NotificationControlsManager.Companion.UNTAGGED_TAG_ID
import com.example.util.simpletimetracker.feature_notification.activitySwitch.manager.NotificationControlsParams
import com.example.util.simpletimetracker.feature_notification.core.TAG_VALUE_DECIMAL_DELIMITER
import com.example.util.simpletimetracker.feature_views.GoalCheckmarkView
import com.example.util.simpletimetracker.feature_views.viewData.RecordTypeIcon
import javax.inject.Inject

class GetNotificationActivitySwitchControlsInteractor @Inject constructor(
    private val iconMapper: IconMapper,
    private val colorMapper: ColorMapper,
    private val recordTypeViewDataMapper: RecordTypeViewDataMapper,
    private val resourceRepo: ResourceRepo,
    private val recordTagViewDataMapper: RecordTagViewDataMapper,
    private val recordTagValueMapper: RecordTagValueMapper,
    private val completeTypesStateInteractor: CompleteTypesStateInteractor,
    private val getSelectableTagsInteractor: GetSelectableTagsInteractor,
    private val recordTagInteractor: RecordTagInteractor,
) {

    suspend fun getControls(
        hint: String,
        isDarkTheme: Boolean,
        types: List<RecordType>,
        suggestions: List<RecordType>,
        showRepeatButton: Boolean,
        isMultipleTagAvailable: Boolean,
        typesShift: Int,
        tagsShift: Int,
        selectedTypeId: Long?,
        selectedTags: List<RecordBase.Tag>,
        editingTagId: Long?,
        editingTagValueInput: String?,
        goals: Map<Long, List<RecordTypeGoal>>,
        allDailyCurrents: Map<Long, GetCurrentRecordsDurationInteractor.Result>,
    ): NotificationControlsParams {
        val viewState = if (editingTagId != null) {
            mapTagSelectionViewState(
                isDarkTheme = isDarkTheme,
                currentValueString = editingTagValueInput,
                valueSuffix = recordTagInteractor.get(editingTagId)
                    ?.valueSuffix.orEmpty(),
            )
        } else {
            mapTypesViewState(
                hint = hint,
                isDarkTheme = isDarkTheme,
                types = types,
                suggestions = suggestions,
                showRepeatButton = showRepeatButton,
                showTagSaveButton = isMultipleTagAvailable,
                typesShift = typesShift,
                selectedTypeId = selectedTypeId,
                selectedTags = selectedTags,
                goals = goals,
                allDailyCurrents = allDailyCurrents,
            )
        }

        return NotificationControlsParams.Enabled(
            typesShift = typesShift,
            tagsShift = tagsShift,
            controlIconColor = colorMapper.toInactiveColor(isDarkTheme),
            isMultipleTagAvailable = isMultipleTagAvailable,
            selectedTypeId = selectedTypeId,
            selectedTags = selectedTags,
            editingTagId = editingTagId,
            editingTagValueInput = editingTagValueInput,
            viewState = viewState,
        )
    }

    private suspend fun mapTypesViewState(
        hint: String,
        isDarkTheme: Boolean,
        types: List<RecordType>,
        suggestions: List<RecordType>,
        showRepeatButton: Boolean,
        showTagSaveButton: Boolean,
        typesShift: Int,
        selectedTypeId: Long?,
        selectedTags: List<RecordBase.Tag>,
        goals: Map<Long, List<RecordTypeGoal>>,
        allDailyCurrents: Map<Long, GetCurrentRecordsDurationInteractor.Result>,
    ): NotificationControlsParams.ViewState {
        val tags = if (selectedTypeId != null && selectedTypeId != 0L) {
            getSelectableTagsInteractor.execute(selectedTypeId)
                .filterNot { it.archived }
        } else {
            emptyList()
        }

        val typesMap = types.associateBy { it.id }

        val selectedTagsMap = selectedTags.associateBy { it.tagId }

        val suggestionsViewData = suggestions.map { type ->
            mapType(
                type = type,
                isDarkTheme = isDarkTheme,
                goals = goals,
                allDailyCurrents = allDailyCurrents,
            )
        }.let {
            populateWithEmpty(
                data = it,
                pageSize = TYPES_LIST_SIZE,
                emptyValueProducer = { NotificationControlsParams.Type.Empty },
            )
        }

        val repeatButtonViewData = if (showRepeatButton) {
            mapRepeat(isDarkTheme)
        } else {
            emptyList()
        }

        val typesViewData = types
            .filter { !it.hidden }
            .map { type ->
                mapType(
                    type = type,
                    isDarkTheme = isDarkTheme,
                    goals = goals,
                    allDailyCurrents = allDailyCurrents,
                )
            }

        val tagsViewData = tags
            .filter { !it.archived }
            .map { tag ->
                mapTag(
                    tag = tag,
                    typesMap = typesMap,
                    selectedTagsMap = selectedTagsMap,
                    isDarkTheme = isDarkTheme,
                )
            }
            .takeUnless { it.isEmpty() }
            ?.let {
                mapApplyTags(isDarkTheme).takeIf { showTagSaveButton }.orEmpty() +
                    mapUntagged(isDarkTheme) + it
            }
            .orEmpty()

        val allTypesViewData = populateWithEmpty(
            data = suggestionsViewData + repeatButtonViewData + typesViewData,
            pageSize = TYPES_LIST_SIZE,
            emptyValueProducer = { NotificationControlsParams.Type.Empty },
        )

        val total = allTypesViewData.size
        val totalPages = total / TYPES_LIST_SIZE
        val currentPage = (typesShift / TYPES_LIST_SIZE) + 1
        val pagesHint = if (total != 0 && totalPages > 1) {
            "($currentPage/$totalPages)"
        } else {
            ""
        }
        val fullHint = if (hint.isNotEmpty()) {
            "$hint $pagesHint"
        } else {
            pagesHint
        }
        return NotificationControlsParams.ViewState.TypeSelection(
            hint = fullHint,
            types = allTypesViewData,
            tags = populateWithEmpty(
                data = tagsViewData,
                pageSize = TAGS_LIST_SIZE,
                emptyValueProducer = { NotificationControlsParams.Tag.Empty },
            ),
            controlIconPrev = RecordTypeIcon.Image(R.drawable.arrow_left),
            controlIconNext = RecordTypeIcon.Image(R.drawable.arrow_right),
            filteredTypeColor = colorMapper.toInactiveColor(isDarkTheme),
        )
    }

    private fun mapTagSelectionViewState(
        isDarkTheme: Boolean,
        currentValueString: String?,
        valueSuffix: String,
    ): NotificationControlsParams.ViewState {
        val hint = when {
            currentValueString.isNullOrEmpty() -> {
                resourceRepo.getString(R.string.change_record_type_value_selection_hint)
            }
            valueSuffix.isEmpty() -> {
                currentValueString
            }
            else -> {
                "$currentValueString $valueSuffix"
            }
        }

        val controlIconColor = colorMapper.toInactiveColor(isDarkTheme)

        val numbers = ((1..9).toList() + 0).map { number ->
            NotificationControlsParams.TagValueControls.Present(
                type = NotificationControlsParams.TagValueControls.Present.Type.Number(number),
                text = number.toString(),
                color = controlIconColor,
            )
        }.plus(
            NotificationControlsParams.TagValueControls.Present(
                type = NotificationControlsParams.TagValueControls.Present.Type.Dot,
                text = TAG_VALUE_DECIMAL_DELIMITER.toString(),
                color = controlIconColor,
            ),
        ).plus(
            NotificationControlsParams.TagValueControls.Present(
                type = NotificationControlsParams.TagValueControls.Present.Type.PlusMinus,
                text = "+/−",
                color = controlIconColor,
            ),
        ).let {
            populateWithEmpty(
                data = it,
                pageSize = 12,
                emptyValueProducer = { NotificationControlsParams.TagValueControls.Empty },
            )
        }

        return NotificationControlsParams.ViewState.TagValueSelection(
            hint = hint,
            numbers = numbers,
            controlIconBack = RecordTypeIcon.Image(R.drawable.record_type_check_cross),
            controlBackColor = resourceRepo.getThemedAttr(R.attr.appNegativeColor, isDarkTheme),
            controlIconSave = RecordTypeIcon.Image(R.drawable.record_type_check_mark),
            controlSaveColor = resourceRepo.getThemedAttr(R.attr.appPositiveColor, isDarkTheme),
            controlIconRemove = RecordTypeIcon.Image(R.drawable.backspace),
        )
    }

    private fun mapType(
        type: RecordType,
        isDarkTheme: Boolean,
        goals: Map<Long, List<RecordTypeGoal>>,
        allDailyCurrents: Map<Long, GetCurrentRecordsDurationInteractor.Result>,
    ): NotificationControlsParams.Type.Present {
        return NotificationControlsParams.Type.Present(
            id = type.id,
            icon = type.icon.let(iconMapper::mapIcon),
            color = type.color.let { colorMapper.mapToColorInt(it, isDarkTheme) },
            checkState = recordTypeViewDataMapper.mapGoalCheckmark(
                type = type,
                goals = goals,
                allDailyCurrents = allDailyCurrents,
            ),
            isComplete = type.id in completeTypesStateInteractor.notificationTypeIds,
        )
    }

    private fun mapRepeat(
        isDarkTheme: Boolean,
    ): List<NotificationControlsParams.Type.Present> {
        val viewData = recordTypeViewDataMapper.mapToRepeatItem(
            numberOfCards = 0,
            isDarkTheme = isDarkTheme,
        )
        return NotificationControlsParams.Type.Present(
            id = REPEAT_BUTTON_ITEM_ID,
            icon = viewData.iconId,
            color = viewData.color,
            checkState = GoalCheckmarkView.CheckState.HIDDEN,
            isComplete = false,
        ).let(::listOf)
    }

    private fun mapTag(
        tag: RecordTag,
        typesMap: Map<Long, RecordType>,
        selectedTagsMap: Map<Long, RecordBase.Tag>,
        isDarkTheme: Boolean,
    ): NotificationControlsParams.Tag {
        return NotificationControlsParams.Tag.Present(
            id = tag.id,
            text = selectedTagsMap[tag.id]?.numericValue?.let { value ->
                recordTagValueMapper.mapTagValue(
                    value = value,
                    valueSuffix = tag.valueSuffix,
                )
            } ?: tag.name,
            color = recordTagViewDataMapper.mapColor(
                tag = tag,
                types = typesMap,
            ).let { colorMapper.mapToColorInt(it, isDarkTheme) },
            isSelected = tag.id in selectedTagsMap.keys,
        )
    }

    private fun mapUntagged(
        isDarkTheme: Boolean,
    ): List<NotificationControlsParams.Tag> {
        return NotificationControlsParams.Tag.Present(
            id = UNTAGGED_TAG_ID,
            text = R.string.change_record_untagged.let(resourceRepo::getString),
            color = colorMapper.toUntrackedColor(isDarkTheme),
            isSelected = false,
        ).let(::listOf)
    }

    private fun mapApplyTags(
        isDarkTheme: Boolean,
    ): List<NotificationControlsParams.Tag> {
        return NotificationControlsParams.Tag.Present(
            id = APPLY_TAGS_ID,
            text = R.string.change_record_save.let(resourceRepo::getString),
            color = resourceRepo.getThemedAttr(R.attr.appActiveColor, isDarkTheme),
            isSelected = false,
        ).let(::listOf)
    }

    // Populate container with empty items to preserve prev next controls position.
    private fun <T> populateWithEmpty(
        data: List<T>,
        pageSize: Int,
        emptyValueProducer: () -> T,
    ): List<T> {
        if (data.isEmpty()) return data
        if (data.size % pageSize == 0) return data
        val emptyCount = pageSize - (data.size % pageSize)
        val emptyData = List(emptyCount) { emptyValueProducer() }
        return data + emptyData
    }
}