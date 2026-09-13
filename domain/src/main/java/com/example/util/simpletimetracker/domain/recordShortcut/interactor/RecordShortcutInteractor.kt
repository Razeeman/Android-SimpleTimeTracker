package com.example.util.simpletimetracker.domain.recordShortcut.interactor

import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.record.model.RecordBase
import com.example.util.simpletimetracker.domain.recordShortcut.model.RecordShortcut
import com.example.util.simpletimetracker.domain.recordShortcut.repo.RecordShortcutRepo
import com.example.util.simpletimetracker.domain.recordTag.repo.RecordShortcutToRecordTagRepo
import com.example.util.simpletimetracker.domain.recordType.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.recordType.model.RecordType
import javax.inject.Inject

class RecordShortcutInteractor @Inject constructor(
    private val repo: RecordShortcutRepo,
    private val recordShortcutToRecordTagRepo: RecordShortcutToRecordTagRepo,
    private val recordTypeInteractor: RecordTypeInteractor,
) {

    suspend fun getAll(): List<RecordShortcut> {
        return sort(
            data = repo.getAll(),
            order = recordTypeInteractor.getAll(),
        )
    }

    suspend fun get(id: Long): RecordShortcut? {
        return repo.get(id)
    }

    suspend fun add(recordShortcut: RecordShortcut) {
        val addedId = repo.add(recordShortcut)
        when (val target = recordShortcut.target) {
            is RecordShortcut.Target.Record -> updateTags(addedId, target.tags)
            is RecordShortcut.Target.Setting -> Unit
        }
    }

    suspend fun update(recordShortcut: RecordShortcut) {
        repo.update(recordShortcut)
        when (val target = recordShortcut.target) {
            is RecordShortcut.Target.Record -> updateTags(recordShortcut.id, target.tags)
            is RecordShortcut.Target.Setting -> recordShortcutToRecordTagRepo.removeAllByShortcutId(recordShortcut.id)
        }
    }

    suspend fun remove(id: Long) {
        recordShortcutToRecordTagRepo.removeAllByShortcutId(id)
        repo.remove(id)
    }

    fun getSettingsOrder(): List<RecordShortcut.SettingAction> {
        return listOf(
            RecordShortcut.SettingAction.Multitasking,
            RecordShortcut.SettingAction.RetroactiveMode,
            RecordShortcut.SettingAction.SortActivities,
            RecordShortcut.SettingAction.Shortcuts,
            RecordShortcut.SettingAction.Categories,
            RecordShortcut.SettingAction.Archive,
            RecordShortcut.SettingAction.DataEdit,
            RecordShortcut.SettingAction.Reminders,
        )
    }

    private suspend fun updateTags(
        shortcutId: Long,
        tags: List<RecordBase.Tag>,
    ) {
        recordShortcutToRecordTagRepo.removeAllByShortcutId(shortcutId)
        recordShortcutToRecordTagRepo.addRecordTags(shortcutId, tags)
    }

    private fun sort(
        data: List<RecordShortcut>,
        order: List<RecordType>,
    ): List<RecordShortcut> {
        val orderMap = order
            .mapIndexed { index, recordType -> recordType.id to index }
            .toMap()
        val settingsOrder = getSettingsOrder()
            .mapIndexed { index, action -> action to index }
            .toMap()

        return data.sortedWith(
            compareBy(
                {
                    when (it.target) {
                        is RecordShortcut.Target.Record -> 0
                        is RecordShortcut.Target.Setting -> 1
                    }
                },
                {
                    when (val target = it.target) {
                        is RecordShortcut.Target.Record -> orderMap[target.typeId].orZero()
                        is RecordShortcut.Target.Setting -> settingsOrder[target.action]
                    }
                },
            ),
        )
    }
}