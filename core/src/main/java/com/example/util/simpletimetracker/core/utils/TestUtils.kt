package com.example.util.simpletimetracker.core.utils

import com.example.util.simpletimetracker.core.mapper.IconMapper
import com.example.util.simpletimetracker.domain.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.domain.model.RecordType
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

class TestUtils @Inject constructor(
    private val recordTypeInteractor: RecordTypeInteractor,
    private val recordInteractor: RecordInteractor,
    private val runningRecordInteractor: RunningRecordInteractor,
    private val iconMapper: IconMapper
) {

    fun clearDatabase() = runBlocking {
        recordTypeInteractor.clear()
        recordInteractor.clear()
        runningRecordInteractor.clear()
    }

    fun populateDatabase() = runBlocking {
        recordTypeInteractor.add(
            RecordType(
                name = "Test1",
                icon = iconMapper.availableIconsNames.keys.elementAt(1),
                color = 1
            )
        )
        recordTypeInteractor.add(
            RecordType(
                name = "Test2",
                icon = iconMapper.availableIconsNames.keys.elementAt(2),
                color = 2
            )
        )
        recordTypeInteractor.add(
            RecordType(
                name = "Test3",
                icon = iconMapper.availableIconsNames.keys.elementAt(3),
                color = 3
            )
        )
    }
}