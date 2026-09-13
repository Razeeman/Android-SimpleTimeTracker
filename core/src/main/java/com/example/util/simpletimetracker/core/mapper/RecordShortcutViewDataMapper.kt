package com.example.util.simpletimetracker.core.mapper

import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.record.model.RecordBase
import com.example.util.simpletimetracker.domain.recordShortcut.model.RecordShortcut
import com.example.util.simpletimetracker.domain.recordTag.model.RecordTag
import com.example.util.simpletimetracker.domain.recordType.model.RecordType
import com.example.util.simpletimetracker.feature_base_adapter.category.CategoryViewData
import com.example.util.simpletimetracker.feature_base_adapter.recordShortcut.RecordShortcutViewData
import com.example.util.simpletimetracker.feature_base_adapter.recordShortcut.RecordShortcutViewData.SpinnerData
import com.example.util.simpletimetracker.feature_views.viewData.RecordTypeIcon
import javax.inject.Inject

class RecordShortcutViewDataMapper @Inject constructor(
    private val resourceRepo: ResourceRepo,
    private val iconMapper: IconMapper,
    private val colorMapper: ColorMapper,
    private val recordTagFullNameMapper: RecordTagFullNameMapper,
) {

    fun map(
        shortcut: RecordShortcut,
        typesMap: Map<Long, RecordType>,
        tags: List<RecordTag>,
        isDarkTheme: Boolean,
        isFiltered: Boolean,
        isEnabled: Boolean,
        spinnerData: SpinnerData? = null,
    ): RecordShortcutViewData {
        return when (val target = shortcut.target) {
            is RecordShortcut.Target.Record -> {
                mapRecord(
                    id = shortcut.id,
                    shortcut = target,
                    recordType = typesMap[target.typeId],
                    recordTags = tags,
                    isDarkTheme = isDarkTheme,
                    isFiltered = isFiltered,
                )
            }
            is RecordShortcut.Target.Setting -> {
                mapSetting(
                    id = shortcut.id,
                    shortcut = target,
                    isDarkTheme = isDarkTheme,
                    isFiltered = isFiltered,
                    isEnabled = isEnabled,
                    spinnerData = spinnerData,
                )
            }
        }
    }

    fun mapActionTitle(action: RecordShortcut.SettingAction): String {
        return when (action) {
            RecordShortcut.SettingAction.Multitasking -> R.string.settings_allow_multitasking
            RecordShortcut.SettingAction.RetroactiveMode -> R.string.settings_retroactive_tracking_mode
            RecordShortcut.SettingAction.Categories -> R.string.categories_title
            RecordShortcut.SettingAction.Archive -> R.string.settings_archive
            RecordShortcut.SettingAction.DataEdit -> R.string.settings_data_edit
            RecordShortcut.SettingAction.SortActivities -> R.string.settings_sort_activity
            RecordShortcut.SettingAction.Shortcuts -> R.string.change_record_shortcut
            RecordShortcut.SettingAction.Reminders -> R.string.settings_reminders_title
        }.let(resourceRepo::getString)
    }

    private fun mapRecord(
        id: Long,
        shortcut: RecordShortcut.Target.Record,
        recordType: RecordType?,
        recordTags: List<RecordTag>,
        isDarkTheme: Boolean,
        isFiltered: Boolean,
    ): RecordShortcutViewData {
        val tagIds = shortcut.tags.map(RecordBase.Tag::tagId)

        val icon = recordType?.icon?.let(iconMapper::mapIcon)

        val color = recordType?.color?.let {
            colorMapper.toFilteredColor(
                color = it,
                isDarkTheme = isDarkTheme,
                isFiltered = isFiltered,
            )
        } ?: colorMapper.toUntrackedColor(isDarkTheme)

        val name = listOf(
            recordType?.name.orEmpty(),
            recordTagFullNameMapper.getFullName(
                tags = recordTags.filter { it.id in tagIds },
                tagData = shortcut.tags,
            ),
            shortcut.comment,
        ).mapNotNull {
            it.takeIf { it.isNotEmpty() }
        }.joinToString(separator = " - ")

        // Shortcut view is simplified category view.
        val data = CategoryViewData.Record.Tagged(
            id = 0,
            name = name,
            iconColor = colorMapper.toIconColor(
                isDarkTheme = isDarkTheme,
                isFiltered = isFiltered,
            ),
            color = color,
            icon = icon,
            iconAlpha = colorMapper.toIconAlpha(icon, isFiltered),
            // TODO show tag with alpha color like in record
        )

        return RecordShortcutViewData(
            id = id,
            hint = resourceRepo.getQuantityString(
                stringResId = R.plurals.statistics_detail_times_tracked,
                quantity = 1,
            ),
            data = data,
        )
    }

    private fun mapSetting(
        id: Long,
        shortcut: RecordShortcut.Target.Setting,
        isDarkTheme: Boolean,
        isFiltered: Boolean,
        isEnabled: Boolean,
        spinnerData: SpinnerData?,
    ): RecordShortcutViewData {
        val icon: RecordTypeIcon? = null
        val name = mapActionTitle(shortcut.action)

        val data = CategoryViewData.Record.Tagged(
            id = id,
            name = if (spinnerData != null) {
                val namePostfix = spinnerData.items.getOrNull(spinnerData.selectedPosition)?.text.orEmpty()
                "$name $namePostfix"
            } else {
                name
            },
            iconColor = colorMapper.toIconColor(
                isDarkTheme = isDarkTheme,
                isFiltered = isFiltered,
            ),
            color = when {
                isEnabled -> resourceRepo.getThemedAttr(R.attr.colorAccent, isDarkTheme)
                isFiltered -> colorMapper.toFilteredColor(isDarkTheme)
                else -> colorMapper.toInactiveColor(isDarkTheme)
            },
            icon = icon,
            iconAlpha = colorMapper.toIconAlpha(icon, isFiltered),
        )

        return RecordShortcutViewData(
            id = id,
            hint = resourceRepo.getString(R.string.shortcut_navigation_settings),
            data = data,
            spinnerData = spinnerData,
        )
    }
}