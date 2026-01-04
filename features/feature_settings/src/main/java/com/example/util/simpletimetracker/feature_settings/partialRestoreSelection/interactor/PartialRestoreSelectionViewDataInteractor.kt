package com.example.util.simpletimetracker.feature_settings.partialRestoreSelection.interactor

import com.example.util.simpletimetracker.core.delegates.iconSelection.mapper.IconSelectionMapper
import com.example.util.simpletimetracker.core.mapper.ActivityFilterViewDataMapper
import com.example.util.simpletimetracker.core.mapper.ActivitySuggestionViewDataMapper
import com.example.util.simpletimetracker.core.mapper.CategoryViewDataMapper
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.ComplexRuleViewDataMapper
import com.example.util.simpletimetracker.core.mapper.DateDividerViewDataMapper
import com.example.util.simpletimetracker.core.mapper.RecordShortcutViewDataMapper
import com.example.util.simpletimetracker.core.mapper.RecordTypeViewDataMapper
import com.example.util.simpletimetracker.core.mapper.RecordViewDataMapper
import com.example.util.simpletimetracker.domain.activityFilter.interactor.ActivityFilterInteractor
import com.example.util.simpletimetracker.domain.category.interactor.CategoryInteractor
import com.example.util.simpletimetracker.domain.favourite.interactor.FavouriteColorInteractor
import com.example.util.simpletimetracker.domain.favourite.interactor.FavouriteCommentInteractor
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.recordTag.interactor.RecordTagInteractor
import com.example.util.simpletimetracker.domain.recordType.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.recordType.interactor.SortCardsInteractor
import com.example.util.simpletimetracker.domain.recordTag.model.CardTagOrder
import com.example.util.simpletimetracker.domain.backup.model.PartialBackupRestoreData
import com.example.util.simpletimetracker.domain.backup.model.getNotExistingValues
import com.example.util.simpletimetracker.domain.recordType.model.CardOrder
import com.example.util.simpletimetracker.domain.recordType.model.RecordType
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.category.CategoryViewData
import com.example.util.simpletimetracker.feature_base_adapter.color.ColorViewData
import com.example.util.simpletimetracker.feature_settings.partialRestore.model.PartialRestoreFilterType
import com.example.util.simpletimetracker.feature_settings.partialRestoreSelection.model.PartialRestoreSelectionDialogParams
import com.example.util.simpletimetracker.feature_views.GoalCheckmarkView
import javax.inject.Inject

class PartialRestoreSelectionViewDataInteractor @Inject constructor(
    private val prefsInteractor: PrefsInteractor,
    private val recordViewDataMapper: RecordViewDataMapper,
    private val recordShortcutViewDataMapper: RecordShortcutViewDataMapper,
    private val recordTypeViewDataMapper: RecordTypeViewDataMapper,
    private val categoryViewDataMapper: CategoryViewDataMapper,
    private val activityFilterViewDataMapper: ActivityFilterViewDataMapper,
    private val complexRuleViewDataMapper: ComplexRuleViewDataMapper,
    private val activitySuggestionViewDataMapper: ActivitySuggestionViewDataMapper,
    private val iconSelectionMapper: IconSelectionMapper,
    private val colorMapper: ColorMapper,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val sortCardsInteractor: SortCardsInteractor,
    private val categoryInteractor: CategoryInteractor,
    private val recordTagInteractor: RecordTagInteractor,
    private val activityFilterInteractor: ActivityFilterInteractor,
    private val favouriteCommentInteractor: FavouriteCommentInteractor,
    private val favouriteColorInteractor: FavouriteColorInteractor,
    private val dateDividerViewDataMapper: DateDividerViewDataMapper,
) {

    suspend fun getViewData(
        extra: PartialRestoreSelectionDialogParams,
        dataIdsFiltered: Set<Long>,
        data: PartialBackupRestoreData,
    ): List<ViewHolderType> {
        val numberOfCards = prefsInteractor.getNumberOfCards()
        val isDarkTheme = prefsInteractor.getDarkMode()
        val useMilitaryTime = prefsInteractor.getUseMilitaryTimeFormat()
        val durationFormat = prefsInteractor.getDurationFormat()
        val showSeconds = prefsInteractor.getShowSeconds()

        return when (extra.type) {
            is PartialRestoreFilterType.Activities -> {
                data.types.values.getNotExistingValues().let {
                    sortTypes(it)
                }.map {
                    recordTypeViewDataMapper.mapFiltered(
                        recordType = it,
                        numberOfCards = numberOfCards,
                        isDarkTheme = isDarkTheme,
                        isFiltered = it.id in dataIdsFiltered,
                        checkState = GoalCheckmarkView.CheckState.HIDDEN,
                        isComplete = false,
                    )
                }
            }
            is PartialRestoreFilterType.Categories -> {
                data.categories.values.getNotExistingValues().let {
                    // Ordered by name.
                    sortCardsInteractor.sort(
                        cardOrder = CardOrder.NAME,
                        manualOrderProvider = { emptyMap() },
                        data = it.map(categoryInteractor::mapForSort),
                    )
                }.map {
                    categoryViewDataMapper.mapCategory(
                        category = it.data,
                        isDarkTheme = isDarkTheme,
                        isFiltered = it.id in dataIdsFiltered,
                    )
                }
            }
            is PartialRestoreFilterType.Tags -> {
                val types = data.types.mapValues { it.value.data }
                val activityOrderProvider = {
                    recordTagInteractor.getActivityOrderProvider(
                        tags = data.tags.values.getNotExistingValues(),
                        typesMap = types,
                        typesToTags = data.typeToTag.map { it.data },
                    )
                }
                data.tags.values.getNotExistingValues().let {
                    // Ordered by activity and activities are sorted by name.
                    sortCardsInteractor.sortTags(
                        cardTagOrder = CardTagOrder.ACTIVITY,
                        manualOrderProvider = { emptyMap() },
                        activityOrderProvider = { activityOrderProvider() },
                        data = it.map { tag ->
                            recordTagInteractor.mapForSort(
                                data = tag,
                                colorSource = types[tag.iconColorSource],
                            )
                        },
                    )
                }.map {
                    categoryViewDataMapper.mapRecordTag(
                        tag = it.data,
                        types = types,
                        isDarkTheme = isDarkTheme,
                        isFiltered = it.id in dataIdsFiltered,
                    )
                }
            }
            is PartialRestoreFilterType.Records -> {
                val typesMap = data.types.mapValues { it.value.data }
                val tags = data.tags.values.map { it.data }
                data.records.values.getNotExistingValues().mapNotNull {
                    val viewData = recordViewDataMapper.map(
                        record = it,
                        recordTypes = typesMap,
                        recordTags = tags,
                        isDarkTheme = isDarkTheme,
                        useMilitaryTime = useMilitaryTime,
                        durationFormat = durationFormat,
                        showSeconds = showSeconds,
                    )?.let { mapped ->
                        recordViewDataMapper.mapFiltered(
                            viewData = mapped,
                            isDarkTheme = isDarkTheme,
                            isFiltered = mapped.id in dataIdsFiltered,
                        )
                    } ?: return@mapNotNull null
                    it.timeStarted to viewData
                }.sortedByDescending { (timeStarted, _) ->
                    timeStarted
                }.let(dateDividerViewDataMapper::addDateViewData)
            }
            is PartialRestoreFilterType.RecordShortcuts -> {
                val typesMap = data.types.mapValues { it.value.data }
                val tags = data.tags.values.map { it.data }
                data.recordShortcuts.values.getNotExistingValues().mapNotNull {
                    recordShortcutViewDataMapper.map(
                        shortcut = it,
                        recordType = typesMap[it.typeId] ?: return@mapNotNull null,
                        recordTags = tags,
                        isDarkTheme = isDarkTheme,
                        isFiltered = it.id in dataIdsFiltered,
                    )
                }
            }
            is PartialRestoreFilterType.ActivityFilters -> {
                data.activityFilters.values.getNotExistingValues().let {
                    activityFilterInteractor.sort(it)
                }.map {
                    activityFilterViewDataMapper.mapFiltered(
                        filter = it,
                        isDarkTheme = isDarkTheme,
                        selected = it.id !in dataIdsFiltered,
                    )
                }
            }
            is PartialRestoreFilterType.FavouriteComments -> {
                data.favouriteComments.values.getNotExistingValues().let {
                    favouriteCommentInteractor.sort(it)
                }.map {
                    val filtered = it.id in dataIdsFiltered
                    CategoryViewData.Category(
                        id = it.id,
                        name = it.comment,
                        iconColor = colorMapper.toIconColor(
                            isDarkTheme = isDarkTheme,
                            isFiltered = filtered,
                        ),
                        color = mapColor(
                            isFiltered = filtered,
                            isDarkTheme = isDarkTheme,
                        ),
                    )
                }
            }
            is PartialRestoreFilterType.FavouriteColors -> {
                data.favouriteColors.values.getNotExistingValues().let {
                    favouriteColorInteractor.sort(it)
                }.map {
                    val filtered = it.id in dataIdsFiltered
                    ColorViewData(
                        colorId = it.id,
                        type = ColorViewData.Type.Favourite,
                        colorInt = it.colorInt.toIntOrNull()
                            ?: colorMapper.toInactiveColor(isDarkTheme),
                        selected = !filtered,
                    )
                }
            }
            is PartialRestoreFilterType.FavouriteIcons -> {
                val icons = data.favouriteIcon.values.getNotExistingValues()
                val iconImages = iconSelectionMapper.mapFavouriteIconImages(
                    icons,
                ).mapNotNull { icon ->
                    val favIcon = icons.firstOrNull { it.icon == icon.iconName }
                        ?: return@mapNotNull null
                    iconSelectionMapper.mapImageViewData(
                        iconName = icon.iconName,
                        iconResId = icon.iconResId,
                        newColor = mapColor(
                            isFiltered = favIcon.id in dataIdsFiltered,
                            isDarkTheme = isDarkTheme,
                        ),
                    )
                }
                val iconEmojis = iconSelectionMapper.mapFavouriteIconEmojis(
                    icons,
                ).mapNotNull { icon ->
                    val favIcon = icons.firstOrNull { it.icon == icon.emojiCode }
                        ?: return@mapNotNull null
                    iconSelectionMapper.mapEmojiViewData(
                        codes = icon.emojiCode,
                        newColor = mapColor(
                            isFiltered = favIcon.id in dataIdsFiltered,
                            isDarkTheme = isDarkTheme,
                        ),
                    )
                }
                iconImages + iconEmojis
            }
            is PartialRestoreFilterType.ComplexRules -> {
                val typesMap = data.types.mapValues { it.value.data }
                val typesOrder = typesMap.values.toList()
                    .let { sortTypes(it) }.map { it.id }
                val tagsMap = data.tags.mapValues { it.value.data }
                // TODO wrong tags order.
                val tagsOrder = data.tags.keys.toList()
                data.rules.values.getNotExistingValues().map { rule ->
                    complexRuleViewDataMapper.mapRuleFiltered(
                        rule = rule,
                        isDarkTheme = isDarkTheme,
                        typesMap = typesMap,
                        tagsMap = tagsMap,
                        typesOrder = typesOrder,
                        tagsOrder = tagsOrder,
                        isFiltered = rule.id in dataIdsFiltered,
                        disableButtonVisible = false,
                    )
                }
            }
            is PartialRestoreFilterType.ActivitySuggestions -> {
                val typesMap = data.types.mapValues { it.value.data }
                val typesOrder = typesMap.values.toList()
                    .let { sortTypes(it) }.map { it.id }
                data.activitySuggestions.values.getNotExistingValues().sortedBy {
                    typesOrder.indexOf(it.forTypeId)
                }.map { suggestion ->
                    activitySuggestionViewDataMapper.mapSuggestionFiltered(
                        suggestion = suggestion,
                        isDarkTheme = isDarkTheme,
                        typesMap = typesMap,
                        typesOrder = typesOrder,
                        isFiltered = suggestion.id in dataIdsFiltered,
                    )
                }
            }
        }
    }

    private fun mapColor(
        isFiltered: Boolean,
        isDarkTheme: Boolean,
    ): Int {
        return if (isFiltered) {
            colorMapper.toInactiveColor(isDarkTheme)
        } else {
            colorMapper.toActiveColor(isDarkTheme)
        }
    }

    private suspend fun sortTypes(
        data: List<RecordType>,
    ): List<RecordType> {
        // Ordered by name.
        return sortCardsInteractor.sort(
            cardOrder = CardOrder.NAME,
            manualOrderProvider = { emptyMap() },
            data = data.map(recordTypeInteractor::mapForSort),
        ).map {
            it.data
        }
    }
}