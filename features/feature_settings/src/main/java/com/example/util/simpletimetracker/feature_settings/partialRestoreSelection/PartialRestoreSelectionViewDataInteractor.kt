package com.example.util.simpletimetracker.feature_settings.partialRestoreSelection

import com.example.util.simpletimetracker.core.delegates.iconSelection.mapper.IconSelectionMapper
import com.example.util.simpletimetracker.core.mapper.ActivityFilterViewDataMapper
import com.example.util.simpletimetracker.core.mapper.CategoryViewDataMapper
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.ComplexRulesViewDataMapper
import com.example.util.simpletimetracker.core.mapper.DateDividerViewDataMapper
import com.example.util.simpletimetracker.core.mapper.RecordTypeViewDataMapper
import com.example.util.simpletimetracker.core.mapper.RecordViewDataMapper
import com.example.util.simpletimetracker.domain.interactor.ActivityFilterInteractor
import com.example.util.simpletimetracker.domain.interactor.CategoryInteractor
import com.example.util.simpletimetracker.domain.interactor.FavouriteColorInteractor
import com.example.util.simpletimetracker.domain.interactor.FavouriteCommentInteractor
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTagInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.interactor.SortCardsInteractor
import com.example.util.simpletimetracker.domain.model.CardOrder
import com.example.util.simpletimetracker.domain.model.CardTagOrder
import com.example.util.simpletimetracker.domain.model.PartialBackupRestoreData
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.category.CategoryViewData
import com.example.util.simpletimetracker.feature_base_adapter.color.ColorViewData
import com.example.util.simpletimetracker.feature_settings.partialRestore.model.PartialRestoreFilterType
import com.example.util.simpletimetracker.feature_settings.partialRestoreSelection.model.PartialRestoreSelectionDialogParams
import javax.inject.Inject

class PartialRestoreSelectionViewDataInteractor @Inject constructor(
    private val prefsInteractor: PrefsInteractor,
    private val recordViewDataMapper: RecordViewDataMapper,
    private val recordTypeViewDataMapper: RecordTypeViewDataMapper,
    private val categoryViewDataMapper: CategoryViewDataMapper,
    private val activityFilterViewDataMapper: ActivityFilterViewDataMapper,
    private val complexRulesViewDataMapper: ComplexRulesViewDataMapper,
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
        val useProportionalMinutes = prefsInteractor.getUseProportionalMinutes()
        val showSeconds = prefsInteractor.getShowSeconds()

        return when (extra.type) {
            is PartialRestoreFilterType.Activities -> {
                data.types.values.let {
                    sortCardsInteractor.sort(
                        cardOrder = CardOrder.NAME,
                        manualOrderProvider = { emptyMap() },
                        data = it.map(recordTypeInteractor::mapForSort),
                    )
                }.map {
                    recordTypeViewDataMapper.mapFiltered(
                        recordType = it.data,
                        numberOfCards = numberOfCards,
                        isDarkTheme = isDarkTheme,
                        isFiltered = it.id in dataIdsFiltered,
                        isChecked = null,
                        isComplete = false,
                    )
                }
            }
            PartialRestoreFilterType.Categories -> {
                data.categories.values.let {
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
            PartialRestoreFilterType.Tags -> {
                val types = data.types
                val activityOrderProvider = {
                    recordTagInteractor.getActivityOrderProvider(
                        tags = data.tags.values.toList(),
                        typesMap = types,
                        typesToTags = data.typeToTag,
                    )
                }
                data.tags.values.let {
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
                        type = types[it.data.iconColorSource],
                        isDarkTheme = isDarkTheme,
                        isFiltered = it.id in dataIdsFiltered,
                    )
                }
            }
            PartialRestoreFilterType.Records -> {
                val typesMap = data.types
                val tags = data.tags.values.toList()
                data.records.values.mapNotNull {
                    val viewData = recordViewDataMapper.mapFilteredRecord(
                        record = it,
                        recordTypes = typesMap,
                        allRecordTags = tags,
                        isDarkTheme = isDarkTheme,
                        useMilitaryTime = useMilitaryTime,
                        useProportionalMinutes = useProportionalMinutes,
                        showSeconds = showSeconds,
                        isFiltered = it.id in dataIdsFiltered,
                    ) ?: return@mapNotNull null
                    it.timeStarted to viewData
                }.sortedByDescending { (timeStarted, _) ->
                    timeStarted
                }.let(dateDividerViewDataMapper::addDateViewData)
            }
            PartialRestoreFilterType.ActivityFilters -> {
                data.activityFilters.values.toList().let {
                    activityFilterInteractor.sort(it)
                }.map {
                    activityFilterViewDataMapper.mapFiltered(
                        filter = it,
                        isDarkTheme = isDarkTheme,
                        selected = it.id !in dataIdsFiltered,
                    )
                }
            }
            PartialRestoreFilterType.FavouriteComments -> {
                data.favouriteComments.values.toList().let {
                    favouriteCommentInteractor.sort(it)
                }.map {
                    val filtered = it.id in dataIdsFiltered
                    CategoryViewData.Category(
                        id = it.id,
                        name = it.comment,
                        iconColor = categoryViewDataMapper.getTextColor(
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
            PartialRestoreFilterType.FavouriteColors -> {
                data.favouriteColors.values.toList().let {
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
            PartialRestoreFilterType.FavouriteIcons -> {
                val icons = data.favouriteIcon
                val iconImages = iconSelectionMapper.mapFavouriteIconImages(
                    icons.values.toList(),
                ).mapNotNull { icon ->
                    val favIcon = icons.values.firstOrNull { it.icon == icon.iconName }
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
                    icons.values.toList(),
                ).mapNotNull { icon ->
                    val favIcon = icons.values.firstOrNull { it.icon == icon.emojiCode }
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
            PartialRestoreFilterType.ComplexRules -> {
                val typesMap = data.types
                val typesOrder = data.types.keys.toList()
                val tagsMap = data.tags
                val tagsOrder = data.tags.keys.toList()
                data.rules.values.map {
                    complexRulesViewDataMapper.mapRuleFiltered(
                        rule = it,
                        isDarkTheme = isDarkTheme,
                        typesMap = typesMap,
                        tagsMap = tagsMap,
                        typesOrder = typesOrder,
                        tagsOrder = tagsOrder,
                        isFiltered = it.id in dataIdsFiltered,
                        disableButtonVisible = false,
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
}