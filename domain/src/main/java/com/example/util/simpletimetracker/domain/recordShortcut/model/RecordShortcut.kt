package com.example.util.simpletimetracker.domain.recordShortcut.model

import com.example.util.simpletimetracker.domain.record.model.RecordBase
import com.example.util.simpletimetracker.domain.widget.model.QuickSettingsWidgetType

data class RecordShortcut(
    val id: Long = 0,
    val target: Target,
) {

    sealed interface Target {
        val mode: TargetMode

        data class Record(
            val typeId: Long,
            val comment: String,
            val tags: List<RecordBase.Tag>,
        ) : Target {
            override val mode: TargetMode = TargetMode.Record
        }

        data class Setting(
            val action: SettingAction,
        ) : Target {
            override val mode: TargetMode = TargetMode.Setting
        }
    }

    enum class TargetMode {
        Record,
        Setting,
    }

    sealed interface SettingAction {
        data class Toggle(val type: QuickSettingsWidgetType) : SettingAction
        object Categories : SettingAction
        object Archive : SettingAction
        object DataEdit : SettingAction
        object SortActivities : SettingAction
        object Shortcuts : SettingAction
        object Reminders : SettingAction
    }
}
