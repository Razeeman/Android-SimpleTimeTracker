package com.example.util.simpletimetracker.core.utils

import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.IconImageMapper
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.activityFilter.interactor.ActivityFilterInteractor
import com.example.util.simpletimetracker.domain.category.interactor.CategoryInteractor
import com.example.util.simpletimetracker.domain.backup.interactor.ClearDataInteractor
import com.example.util.simpletimetracker.domain.complexRule.interactor.ComplexRuleInteractor
import com.example.util.simpletimetracker.domain.favourite.interactor.FavouriteColorInteractor
import com.example.util.simpletimetracker.domain.favourite.interactor.FavouriteCommentInteractor
import com.example.util.simpletimetracker.domain.favourite.interactor.FavouriteIconInteractor
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.record.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.recordTag.interactor.RecordTagInteractor
import com.example.util.simpletimetracker.domain.category.interactor.RecordTypeCategoryInteractor
import com.example.util.simpletimetracker.domain.recordType.interactor.RecordTypeGoalInteractor
import com.example.util.simpletimetracker.domain.recordType.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.recordTag.interactor.RecordTypeToDefaultTagInteractor
import com.example.util.simpletimetracker.domain.recordTag.interactor.RecordTypeToTagInteractor
import com.example.util.simpletimetracker.domain.record.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.domain.activityFilter.model.ActivityFilter
import com.example.util.simpletimetracker.domain.activitySuggestion.interactor.ActivitySuggestionInteractor
import com.example.util.simpletimetracker.domain.activitySuggestion.model.ActivitySuggestion
import com.example.util.simpletimetracker.domain.category.model.Category
import com.example.util.simpletimetracker.domain.color.model.AppColor
import com.example.util.simpletimetracker.domain.complexRule.model.ComplexRule
import com.example.util.simpletimetracker.domain.daysOfWeek.model.DayOfWeek
import com.example.util.simpletimetracker.domain.extension.ifNull
import com.example.util.simpletimetracker.domain.favourite.model.FavouriteColor
import com.example.util.simpletimetracker.domain.favourite.model.FavouriteComment
import com.example.util.simpletimetracker.domain.favourite.model.FavouriteIcon
import com.example.util.simpletimetracker.domain.record.model.Record
import com.example.util.simpletimetracker.domain.record.model.RecordBase
import com.example.util.simpletimetracker.domain.recordTag.model.RecordTag
import com.example.util.simpletimetracker.domain.recordType.model.RecordType
import com.example.util.simpletimetracker.domain.recordType.model.RecordTypeGoal
import com.example.util.simpletimetracker.domain.record.model.RunningRecord
import com.example.util.simpletimetracker.domain.recordShortcut.interactor.RecordShortcutInteractor
import com.example.util.simpletimetracker.domain.recordShortcut.model.RecordShortcut
import com.example.util.simpletimetracker.domain.recordTag.model.RecordTagValueType
import kotlinx.coroutines.runBlocking
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class TestUtils @Inject constructor(
    val recordTypeInteractor: RecordTypeInteractor,
    val recordInteractor: RecordInteractor,
    private val runningRecordInteractor: RunningRecordInteractor,
    private val categoryInteractor: CategoryInteractor,
    private val recordTypeCategoryInteractor: RecordTypeCategoryInteractor,
    private val recordTagInteractor: RecordTagInteractor,
    private val recordTypeToTagInteractor: RecordTypeToTagInteractor,
    private val recordTypeToDefaultTagInteractor: RecordTypeToDefaultTagInteractor,
    private val activityFilterInteractor: ActivityFilterInteractor,
    private val recordTypeGoalInteractor: RecordTypeGoalInteractor,
    private val favouriteCommentInteractor: FavouriteCommentInteractor,
    private val favouriteIconInteractor: FavouriteIconInteractor,
    private val favouriteColorInteractor: FavouriteColorInteractor,
    private val complexRuleInteractor: ComplexRuleInteractor,
    val activitySuggestionInteractor: ActivitySuggestionInteractor,
    private val recordShortcutInteractor: RecordShortcutInteractor,
    private val prefsInteractor: PrefsInteractor,
    private val iconImageMapper: IconImageMapper,
    private val clearDataInteractor: ClearDataInteractor,
) {

    fun clearDatabase() = runBlocking {
        clearDataInteractor.execute()
    }

    fun clearPrefs() = runBlocking {
        prefsInteractor.clear()
    }

    fun setFirstDayOfWeek(day: DayOfWeek) = runBlocking {
        prefsInteractor.setFirstDayOfWeek(day)
    }

    fun addActivity(
        name: String,
        color: Int? = null,
        colorInt: Int? = null,
        icon: Int? = null,
        text: String? = null,
        goals: List<RecordTypeGoal> = emptyList(),
        archived: Boolean = false,
        defaultDuration: Long = 0,
        note: String = "",
        categories: List<String> = emptyList(),
    ) = runBlocking {
        val icons = iconImageMapper
            .getAvailableImages(loadSearchHints = false).values
            .flatten().associateBy { it.iconName }.mapValues { it.value.iconResId }
        val iconId = icons.filterValues { it == icon }.keys.firstOrNull()
            ?: text
            ?: icons.keys.first()

        val colors = ColorMapper.getAvailableColors()
        val colorId = colors.indexOf(color).takeUnless { it == -1 }
            ?: (0..colors.size).random()

        val availableCategories = categoryInteractor.getAll()

        val data = RecordType(
            name = name,
            color = AppColor(colorId = colorId, colorInt = colorInt?.toString().orEmpty()),
            icon = iconId,
            defaultDuration = defaultDuration,
            note = note,
            hidden = archived,
        )

        val typeId = recordTypeInteractor.add(data)

        categories
            .mapNotNull { categoryName ->
                availableCategories.firstOrNull { it.name == categoryName }?.id
            }
            .takeUnless {
                it.isEmpty()
            }
            ?.let { categoryIds ->
                recordTypeCategoryInteractor.addCategories(typeId, categoryIds)
            }

        goals.forEach {
            recordTypeGoalInteractor.add(it.copy(idData = RecordTypeGoal.IdData.Type(typeId)))
        }
    }

    fun addRecord(
        typeName: String,
        timeStarted: Long? = null,
        timeEnded: Long? = null,
        tagNames: List<String> = emptyList(),
        tagNamesWithValues: List<Pair<String, Double>> = emptyList(),
        comment: String = "",
    ) = runBlocking {
        val type = recordTypeInteractor.getAll().firstOrNull { it.name == typeName }
            ?: return@runBlocking
        val tags = recordTagInteractor.getAll().associateBy { it.name }
        val tagsData = tagNames
            .takeUnless { it.isEmpty() }
            ?.map { it to null }
            .ifNull { tagNamesWithValues }
            .mapNotNull { (name, value) ->
                RecordBase.Tag(
                    tagId = tags[name]?.id ?: return@mapNotNull null,
                    numericValue = value,
                )
            }

        val data = Record(
            typeId = type.id,
            timeStarted = timeStarted
                ?: (System.currentTimeMillis() - TimeUnit.HOURS.toMillis(1)),
            timeEnded = timeEnded
                ?: System.currentTimeMillis(),
            comment = comment,
            tags = tagsData,
        )

        recordInteractor.add(data)
    }

    fun addRunningRecord(
        typeName: String,
        timeStarted: Long? = null,
        tagNames: List<String> = emptyList(),
        comment: String = "",
    ) = runBlocking {
        val type = recordTypeInteractor.getAll().firstOrNull { it.name == typeName }
            ?: return@runBlocking
        val tagIds = recordTagInteractor.getAll().filter { it.name in tagNames }
            .map { it.id }

        val data = RunningRecord(
            id = type.id,
            timeStarted = timeStarted ?: System.currentTimeMillis(),
            comment = comment,
            tags = tagIds.map {
                RecordBase.Tag(
                    tagId = it,
                    numericValue = null,
                )
            },
        )

        runningRecordInteractor.add(data)
    }

    fun addCategory(
        tagName: String,
        color: Int? = null,
        note: String = "",
        goals: List<RecordTypeGoal> = emptyList(),
    ) = runBlocking {
        val colors = ColorMapper.getAvailableColors()
        val colorId = colors.indexOf(color).takeUnless { it == -1 }
            ?: (0..colors.size).random()

        val data = Category(
            name = tagName,
            color = AppColor(colorId = colorId, colorInt = ""),
            note = note,
        )

        val categoryId = categoryInteractor.add(data)

        goals.forEach {
            recordTypeGoalInteractor.add(it.copy(idData = RecordTypeGoal.IdData.Category(categoryId)))
        }
    }

    fun addRecordTag(
        tagName: String,
        typeName: String? = null,
        archived: Boolean = false,
        color: Int? = null,
        icon: Int? = null,
        note: String = "",
        defaultTypes: List<String> = emptyList(),
        hasTagValue: Boolean = false,
        tagValueSuffix: String = "",
        goals: List<RecordTypeGoal> = emptyList(),
    ) = runBlocking {
        val type = recordTypeInteractor.getAll().firstOrNull { it.name == typeName }

        val colors = ColorMapper.getAvailableColors()
        val colorId = colors.indexOf(color).takeUnless { it == -1 }
            ?: (0..colors.size).random()

        val icons = iconImageMapper
            .getAvailableImages(loadSearchHints = false).values
            .flatten().associateBy { it.iconName }.mapValues { it.value.iconResId }
        val iconId = icons.filterValues { it == icon }.keys.firstOrNull()

        val data = RecordTag(
            name = tagName,
            icon = iconId.orEmpty(),
            color = AppColor(colorId = colorId, colorInt = ""),
            iconColorSource = type?.id.orZero(),
            note = note,
            archived = archived,
            valueType = if (hasTagValue) RecordTagValueType.NUMERIC else RecordTagValueType.NONE,
            valueSuffix = tagValueSuffix,
        )

        val tagId = recordTagInteractor.add(data)
        val types = recordTypeInteractor.getAll()

        types
            .firstOrNull { it.name == typeName }
            ?.id
            ?.let {
                recordTypeToTagInteractor.addTypes(tagId = tagId, typeIds = listOf(it))
            }

        types.filter { it.name in defaultTypes }
            .map { it.id }
            .takeUnless { it.isEmpty() }
            ?.let { recordTypeToDefaultTagInteractor.addTypes(tagId = tagId, typeIds = it) }

        goals.forEach {
            recordTypeGoalInteractor.add(it.copy(idData = RecordTypeGoal.IdData.Tag(tagId)))
        }
    }

    fun addActivityFilter(
        name: String,
        type: ActivityFilter.Type = ActivityFilter.Type.Activity,
        color: Int? = null,
        colorInt: Int? = null,
        names: List<String> = emptyList(),
        selected: Boolean = false,
    ) = runBlocking {
        val colors = ColorMapper.getAvailableColors()
        val colorId = colors.indexOf(color).takeUnless { it == -1 }
            ?: (0..colors.size).random()
        val availableCategories = categoryInteractor.getAll()
        val availableTypes = recordTypeInteractor.getAll()
        val selectedIds = names.mapNotNull { name ->
            when (type) {
                is ActivityFilter.Type.Activity -> {
                    availableTypes.firstOrNull { it.name == name }?.id
                }

                is ActivityFilter.Type.Category -> {
                    availableCategories.firstOrNull { it.name == name }?.id
                }
            }
        }.toSet()

        val data = ActivityFilter(
            selectedIds = selectedIds,
            type = type,
            name = name,
            color = AppColor(colorId = colorId, colorInt = colorInt?.toString().orEmpty()),
            selected = selected,
        )

        activityFilterInteractor.add(data)
    }

    fun addFavouriteComment(
        text: String,
    ) = runBlocking {
        favouriteCommentInteractor.add(FavouriteComment(comment = text))
    }

    fun addFavouriteIcon(
        text: String,
    ) = runBlocking {
        favouriteIconInteractor.add(FavouriteIcon(icon = text))
    }

    fun addFavouriteColor(
        colorInt: Int,
    ) = runBlocking {
        favouriteColorInteractor.add(FavouriteColor(colorInt = colorInt.toString()))
    }

    fun addComplexRule(
        action: ComplexRule.Action,
        assignTagNames: List<String> = emptyList(),
        startingTypeNames: List<String> = emptyList(),
        currentTypeNames: List<String> = emptyList(),
        daysOfWeek: List<DayOfWeek> = emptyList(),
    ) = runBlocking {
        val availableTypes = recordTypeInteractor.getAll()
        val assignTagIds = recordTagInteractor.getAll()
            .filter { it.name in assignTagNames }
            .map { it.id }
            .toSet()

        val data = ComplexRule(
            disabled = false,
            action = action,
            actionAssignTagIds = assignTagIds,
            conditionStartingTypeIds = getTypeIds(availableTypes, startingTypeNames),
            conditionCurrentTypeIds = getTypeIds(availableTypes, currentTypeNames),
            conditionDaysOfWeek = daysOfWeek.toSet(),
        )

        complexRuleInteractor.add(data)
    }

    fun addSuggestion(
        forTypeName: String,
        names: List<String> = emptyList(),
    ) = runBlocking {
        val availableTypes = recordTypeInteractor.getAll()

        val data = ActivitySuggestion(
            forTypeId = getTypeIds(
                availableTypes = availableTypes,
                names = listOf(forTypeName),
            ).firstOrNull() ?: return@runBlocking,
            suggestionIds = getTypeIds(
                availableTypes = availableTypes,
                names = names,
            ),
        )

        activitySuggestionInteractor.add(listOf(data))
    }

    fun addShortcut(
        typeName: String,
        tagNames: List<String> = emptyList(),
        comment: String = "",
    ) = runBlocking {
        val type = recordTypeInteractor.getAll().firstOrNull { it.name == typeName }
            ?: return@runBlocking
        val tagIds = recordTagInteractor.getAll().filter { it.name in tagNames }
            .map { it.id }

        val data = RecordShortcut(
            typeId = type.id,
            comment = comment,
            tags = tagIds.map {
                RecordBase.Tag(
                    tagId = it,
                    numericValue = null,
                )
            },
        )

        recordShortcutInteractor.add(data)
    }

    suspend fun getRunningRecords(): List<RunningRecord> {
        return runningRecordInteractor.getAll()
    }

    private fun getTypeIds(
        availableTypes: List<RecordType>,
        names: List<String>,
    ): Set<Long> {
        return names.mapNotNull { name ->
            availableTypes.firstOrNull { it.name == name }?.id
        }.toSet()
    }
}