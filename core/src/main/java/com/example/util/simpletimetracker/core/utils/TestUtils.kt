package com.example.util.simpletimetracker.core.utils

import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.IconMapper
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.interactor.CategoryInteractor
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeCategoryInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.domain.model.Record
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
    private val prefsInteractor: PrefsInteractor,
    private val iconMapper: IconMapper
) {

    fun clearDatabase() = runBlocking {
        recordTypeInteractor.clear()
        recordInteractor.clear()
        runningRecordInteractor.clear()
        categoryInteractor.clear()
        recordTypeCategoryInteractor.clear()
    }

    fun clearPrefs() = runBlocking {
        prefsInteractor.clear()
    }

    fun addActivity(
        name: String,
        color: Int? = null,
        icon: Int? = null,
        goalTime: Long? = null,
        categories: List<String> = emptyList()
    ) = runBlocking {
        val icons = iconMapper.availableIconsNames
        val iconId = icons.filterValues { it == icon }.keys.firstOrNull()
            ?: icons.keys.first()

        val colors = ColorMapper.getAvailableColors(false)
        val colorId = colors.indexOf(color).takeUnless { it == -1 }.orZero()

        val availableCategories = categoryInteractor.getAll()

        val data = RecordType(
            name = name,
            color = colorId,
            icon = iconId,
            goalTime = goalTime.orZero()
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
        timeEnded: Long? = null
    ) = runBlocking {
        val type = recordTypeInteractor.getAll().firstOrNull { it.name == typeName }
            ?: return@runBlocking

        val data = Record(
            typeId = type.id,
            timeStarted = timeStarted
                ?: (System.currentTimeMillis() - TimeUnit.HOURS.toMillis(1)),
            timeEnded = timeEnded
                ?: System.currentTimeMillis(),
            comment = ""
        )

        recordInteractor.add(data)
    }
}