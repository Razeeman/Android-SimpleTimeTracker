package com.example.util.simpletimetracker.core.utils

import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.IconImageMapper
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.interactor.CategoryInteractor
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTagInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeCategoryInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.domain.model.Category
import com.example.util.simpletimetracker.domain.model.DayOfWeek
import com.example.util.simpletimetracker.domain.model.Record
import com.example.util.simpletimetracker.domain.model.RecordTag
import com.example.util.simpletimetracker.domain.model.RecordType
import kotlinx.coroutines.runBlocking
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class TestUtils @Inject constructor(
    private val recordTypeInteractor: RecordTypeInteractor,
    private val recordInteractor: RecordInteractor,
    private val runningRecordInteractor: RunningRecordInteractor,
    private val categoryInteractor: CategoryInteractor,
    private val recordTypeCategoryInteractor: RecordTypeCategoryInteractor,
    private val recordTagInteractor: RecordTagInteractor,
    private val prefsInteractor: PrefsInteractor,
    private val iconImageMapper: IconImageMapper
) {

    fun clearDatabase() = runBlocking {
        recordTypeInteractor.clear()
        recordInteractor.clear()
        runningRecordInteractor.clear()
        categoryInteractor.clear()
        recordTypeCategoryInteractor.clear()
        recordTagInteractor.clear()
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
        icon: Int? = null,
        emoji: String? = null,
        goalTime: Long? = null,
        archived: Boolean = false,
        categories: List<String> = emptyList()
    ) = runBlocking {
        val icons = iconImageMapper.availableIconsNames
        val iconId = icons.filterValues { it == icon }.keys.firstOrNull()
            ?: emoji
            ?: icons.keys.first()

        val colors = ColorMapper.getAvailableColors(false)
        val colorId = colors.indexOf(color).takeUnless { it == -1 }
            ?: (0..colors.size).random()

        val availableCategories = categoryInteractor.getAll()

        val data = RecordType(
            name = name,
            color = colorId,
            icon = iconId,
            goalTime = goalTime.orZero(),
            hidden = archived
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
    }

    fun addRecord(
        typeName: String,
        timeStarted: Long? = null,
        timeEnded: Long? = null,
        tagName: String? = null
    ) = runBlocking {
        val type = recordTypeInteractor.getAll().firstOrNull { it.name == typeName }
            ?: return@runBlocking
        val tag = recordTagInteractor.getAll().firstOrNull { it.name == tagName }

        val data = Record(
            typeId = type.id,
            timeStarted = timeStarted
                ?: (System.currentTimeMillis() - TimeUnit.HOURS.toMillis(1)),
            timeEnded = timeEnded
                ?: System.currentTimeMillis(),
            comment = "",
            tagId = tag?.id.orZero()
        )

        recordInteractor.add(data)
    }

    fun addActivityTag(
        tagName: String
    ) = runBlocking {
        val data = Category(
            name = tagName,
            color = 0
        )

        categoryInteractor.add(data)
    }

    fun addRecordTag(
        typeName: String,
        tagName: String,
        archived: Boolean = false
    ) = runBlocking {
        val type = recordTypeInteractor.getAll().firstOrNull { it.name == typeName }
            ?: return@runBlocking

        val data = RecordTag(
            typeId = type.id,
            name = tagName,
            archived = archived
        )

        recordTagInteractor.add(data)
    }

    fun getTypeId(typeName: String): Long = runBlocking {
        recordTypeInteractor.getAll()
            .firstOrNull { it.name == typeName }
            ?.id.orZero()
    }
}