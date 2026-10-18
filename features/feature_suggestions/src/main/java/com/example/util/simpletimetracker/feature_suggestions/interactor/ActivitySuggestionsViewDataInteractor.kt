package com.example.util.simpletimetracker.feature_suggestions.interactor

import com.example.util.simpletimetracker.core.mapper.RecordTypeViewDataMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.extension.plusAssign
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.recordType.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.recordType.model.RecordType
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.button.ButtonViewData
import com.example.util.simpletimetracker.feature_base_adapter.divider.DividerViewData
import com.example.util.simpletimetracker.feature_base_adapter.emptySpace.EmptySpaceViewData
import com.example.util.simpletimetracker.feature_base_adapter.hint.HintViewData
import com.example.util.simpletimetracker.feature_base_adapter.recordTypeRelation.ActivitySuggestionListViewData
import com.example.util.simpletimetracker.feature_base_adapter.recordTypeSpecial.RunningRecordTypeSpecialViewData
import com.example.util.simpletimetracker.feature_suggestions.viewData.ActivitySuggestionsButtonViewData
import com.example.util.simpletimetracker.feature_suggestions.R
import com.example.util.simpletimetracker.feature_suggestions.adapter.ActivitySuggestionSpecialViewData
import com.example.util.simpletimetracker.feature_views.viewData.RecordTypeIcon
import javax.inject.Inject

class ActivitySuggestionsViewDataInteractor @Inject constructor(
    private val resourceRepo: ResourceRepo,
    private val prefsInteractor: PrefsInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val recordTypeViewDataMapper: RecordTypeViewDataMapper,
) {

    suspend fun getViewData(
        suggestionsMap: Map<Long, Set<Long>>,
    ): List<ViewHolderType> {
        val isDarkTheme = prefsInteractor.getDarkMode()
        val recordTypes = recordTypeInteractor.getAll().filter { !it.hidden }
        val typesOrder = recordTypes.map(RecordType::id)
        val recordTypesMap = recordTypes.associateBy(RecordType::id)
        val selectedActivities = suggestionsMap.keys

        val result: MutableList<ViewHolderType> = mutableListOf()

        result += ButtonViewData(
            id = ActivitySuggestionsButtonViewData(
                block = ActivitySuggestionsButtonViewData.Block.ADD,
            ),
            text = resourceRepo.getString(R.string.change_record_message_choose_type),
            icon = ButtonViewData.Icon.Present(
                icon = R.drawable.action_change_item,
                iconColor = resourceRepo.getThemedAttr(R.attr.appLightTextColor, isDarkTheme),
                iconBackgroundColor = resourceRepo.getColor(R.color.transparent),
            ),
            backgroundColor = resourceRepo.getThemedAttr(R.attr.appInactiveColor, isDarkTheme),
            isEnabled = true,
            marginHorizontalDp = 0,
        )

        if (selectedActivities.isNotEmpty()) {
            result += ButtonViewData(
                id = ActivitySuggestionsButtonViewData(
                    block = ActivitySuggestionsButtonViewData.Block.CALCULATE,
                ),
                text = resourceRepo.getString(R.string.activity_suggestions_calculate),
                icon = ButtonViewData.Icon.Present(
                    icon = R.drawable.statistics,
                    iconColor = resourceRepo.getThemedAttr(R.attr.appLightTextColor, isDarkTheme),
                    iconBackgroundColor = resourceRepo.getColor(R.color.transparent),
                ),
                backgroundColor = resourceRepo.getThemedAttr(R.attr.appInactiveColor, isDarkTheme),
                isEnabled = true,
                marginHorizontalDp = 0,
            )
        }

        selectedActivities.sortedBy {
            typesOrder.indexOf(it).toLong()
        }.forEachIndexed { index, typeId ->
            if (index == 0) {
                result += DividerViewData(id = 0)
            }
            if (index == 0) {
                result += HintViewData(
                    text = resourceRepo.getString(R.string.change_record_type_field),
                    paddingTop = 0,
                    paddingBottom = 0,
                    gravity = HintViewData.Gravity.START,
                )
            }
            result += recordTypeViewDataMapper.map(
                recordType = recordTypesMap[typeId] ?: return@forEachIndexed,
                numberOfCards = 0, // Use default size.
                isDarkTheme = isDarkTheme,
            )
            result += EmptySpaceViewData(
                id = typeId,
                wrapBefore = true,
            )
            if (index == 0) {
                result += HintViewData(
                    text = resourceRepo.getString(R.string.settings_activity_suggestions) +
                        " " +
                        resourceRepo.getString(R.string.card_order_hint).let { "($it)" },
                    paddingTop = 0,
                    paddingBottom = 0,
                    gravity = HintViewData.Gravity.START,
                )
            }
            val thisTypeSuggestions = suggestionsMap[typeId].orEmpty()
            thisTypeSuggestions.forEach { suggestion ->
                result += mapSuggestion(
                    forTypeId = typeId,
                    suggestionTypeId = suggestion,
                    recordTypesMap = recordTypesMap,
                    isDarkTheme = isDarkTheme,
                )
            }
            result += mapAddSuggestionButton(
                forTypeId = typeId,
                hasAtLeastOneEntry = thisTypeSuggestions.isNotEmpty(),
                isDarkTheme = isDarkTheme,
            )
            result += mapToCalculateSuggestionButton(
                forTypeId = typeId,
                isDarkTheme = isDarkTheme,
            )
            if (index < selectedActivities.size - 1) {
                result += DividerViewData(id = typeId)
            }
        }

        return result
    }

    private fun mapSuggestion(
        forTypeId: Long,
        suggestionTypeId: Long,
        recordTypesMap: Map<Long, RecordType>,
        isDarkTheme: Boolean,
    ): ActivitySuggestionListViewData? {
        return recordTypeViewDataMapper.map(
            recordType = recordTypesMap[suggestionTypeId] ?: return null,
            numberOfCards = 0, // Use default size.
            isDarkTheme = isDarkTheme,
        ).let {
            ActivitySuggestionListViewData(
                id = ActivitySuggestionListViewData.Id(
                    suggestionTypeId = suggestionTypeId,
                    forTypeId = forTypeId,
                ),
                text = it.name,
                icon = it.iconId,
                // TODO GRADIENT
                color = it.color.primaryColor,
            )
        }
    }

    private fun mapAddSuggestionButton(
        forTypeId: Long,
        hasAtLeastOneEntry: Boolean,
        isDarkTheme: Boolean,
    ): ActivitySuggestionSpecialViewData {
        return mapAddButton(
            hasAtLeastOneEntry = hasAtLeastOneEntry,
            isDarkTheme = isDarkTheme,
        ).let {
            ActivitySuggestionSpecialViewData(
                id = ActivitySuggestionSpecialViewData.Id(
                    forTypeId = forTypeId,
                    type = ActivitySuggestionSpecialViewData.Type.Add,
                ),
                data = ActivitySuggestionListViewData(
                    id = ActivitySuggestionListViewData.Id(0, 0),
                    text = it.name,
                    icon = it.iconId,
                    // TODO GRADIENT
                    color = it.color.primaryColor,
                ),
            )
        }
    }

    private fun mapToCalculateSuggestionButton(
        forTypeId: Long,
        isDarkTheme: Boolean,
    ): ActivitySuggestionSpecialViewData {
        return recordTypeViewDataMapper.mapToAddItem(
            numberOfCards = 0, // Use default size.
            isDarkTheme = isDarkTheme,
        ).copy(
            name = resourceRepo.getString(R.string.shortcut_navigation_statistics),
            iconId = RecordTypeIcon.Image(R.drawable.statistics),
        ).let {
            ActivitySuggestionSpecialViewData(
                id = ActivitySuggestionSpecialViewData.Id(
                    forTypeId = forTypeId,
                    type = ActivitySuggestionSpecialViewData.Type.Calculate,
                ),
                data = ActivitySuggestionListViewData(
                    id = ActivitySuggestionListViewData.Id(0, 0),
                    text = it.name,
                    icon = it.iconId,
                    // TODO GRADIENT
                    color = it.color.primaryColor,
                ),
            )
        }
    }

    private fun mapAddButton(
        hasAtLeastOneEntry: Boolean,
        isDarkTheme: Boolean,
    ): RunningRecordTypeSpecialViewData {
        val name = if (hasAtLeastOneEntry) {
            R.string.data_edit_button_change
        } else {
            R.string.running_records_add_type
        }.let(resourceRepo::getString)

        val iconId = if (hasAtLeastOneEntry) {
            R.drawable.action_change_item
        } else {
            R.drawable.add
        }.let(RecordTypeIcon::Image)

        return recordTypeViewDataMapper.mapToAddItem(
            numberOfCards = 0, // Use default size.
            isDarkTheme = isDarkTheme,
        ).copy(
            name = name,
            iconId = iconId,
        )
    }
}