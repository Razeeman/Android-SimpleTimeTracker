package com.example.util.simpletimetracker.data_local.recordShortcut

import com.example.util.simpletimetracker.data_local.recordTag.RecordShortcutToRecordTagDBO
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.record.model.RecordBase
import com.example.util.simpletimetracker.domain.recordShortcut.model.RecordShortcut
import javax.inject.Inject

class RecordShortcutDataLocalMapper @Inject constructor() {

    fun map(dbo: RecordShortcutWithRecordTagsDBO): RecordShortcut {
        val target = when (dbo.shortcut.targetType) {
            1L -> RecordShortcut.Target.Setting(
                action = mapSettingAction(
                    dbo = dbo.shortcut.settingAction,
                ),
            )
            else -> RecordShortcut.Target.Record(
                typeId = dbo.shortcut.typeId,
                comment = dbo.shortcut.comment,
                tags = dbo.recordTags.map(::map),
            )
        }

        return RecordShortcut(
            id = dbo.shortcut.id,
            target = target,
        )
    }

    fun map(domain: RecordShortcut): RecordShortcutDBO {
        val recordTarget = domain.target as? RecordShortcut.Target.Record
        val settingTarget = domain.target as? RecordShortcut.Target.Setting
        val targetType = when (domain.target) {
            is RecordShortcut.Target.Record -> 0L
            is RecordShortcut.Target.Setting -> 1L
        }

        return RecordShortcutDBO(
            id = domain.id,
            typeId = recordTarget?.typeId.orZero(),
            comment = recordTarget?.comment.orEmpty(),
            targetType = targetType,
            settingAction = settingTarget?.action
                ?.let(::mapSettingAction).orZero(),
        )
    }

    private fun map(dbo: RecordShortcutToRecordTagDBO): RecordBase.Tag {
        return RecordBase.Tag(
            tagId = dbo.recordTagId,
            numericValue = dbo.recordTagNumericValue,
        )
    }

    fun mapSettingAction(dbo: Long?): RecordShortcut.SettingAction {
        return when (dbo) {
            0L -> RecordShortcut.SettingAction.Multitasking
            1L -> RecordShortcut.SettingAction.RetroactiveMode
            2L -> RecordShortcut.SettingAction.Categories
            3L -> RecordShortcut.SettingAction.Archive
            4L -> RecordShortcut.SettingAction.DataEdit
            5L -> RecordShortcut.SettingAction.SortActivities
            6L -> RecordShortcut.SettingAction.Shortcuts
            7L -> RecordShortcut.SettingAction.Reminders
            else -> RecordShortcut.SettingAction.Multitasking
        }
    }

    fun mapSettingAction(domain: RecordShortcut.SettingAction): Long {
        return when (domain) {
            RecordShortcut.SettingAction.Multitasking -> 0L
            RecordShortcut.SettingAction.RetroactiveMode -> 1L
            RecordShortcut.SettingAction.Categories -> 2L
            RecordShortcut.SettingAction.Archive -> 3L
            RecordShortcut.SettingAction.DataEdit -> 4L
            RecordShortcut.SettingAction.SortActivities -> 5L
            RecordShortcut.SettingAction.Shortcuts -> 6L
            RecordShortcut.SettingAction.Reminders -> 7L
        }
    }
}