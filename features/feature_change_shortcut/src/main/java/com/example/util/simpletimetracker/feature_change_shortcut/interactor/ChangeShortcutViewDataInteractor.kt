package com.example.util.simpletimetracker.feature_change_shortcut.interactor

import com.example.util.simpletimetracker.domain.widget.model.QuickSettingsWidgetType
import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.IconMapper
import com.example.util.simpletimetracker.core.mapper.RecordShortcutViewDataMapper
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.record.model.RecordBase
import com.example.util.simpletimetracker.domain.recordShortcut.model.RecordShortcut
import com.example.util.simpletimetracker.domain.recordTag.model.RecordTag
import com.example.util.simpletimetracker.domain.recordType.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.recordType.model.RecordType
import com.example.util.simpletimetracker.feature_change_shortcut.mapper.ChangeShortcutViewDataMapper
import com.example.util.simpletimetracker.feature_change_shortcut.viewData.ChangeShortcutViewData
import com.example.util.simpletimetracker.feature_views.viewData.RecordTypeIcon
import javax.inject.Inject

class ChangeShortcutViewDataInteractor @Inject constructor(
    private val prefsInteractor: PrefsInteractor,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val viewDataMapper: ChangeShortcutViewDataMapper,
    private val colorMapper: ColorMapper,
    private val iconMapper: IconMapper,
    private val recordShortcutViewDataMapper: RecordShortcutViewDataMapper,
) {

    suspend fun getViewData(
        params: Params,
        typesMap: Map<Long, RecordType>,
        tags: List<RecordTag>,
    ): ChangeShortcutViewData {
        val isDarkTheme = prefsInteractor.getDarkMode()
        val type = params.recordTypeId?.let { recordTypeInteractor.get(it) }
        val target = buildTarget(params)
        val shortcutPreview = RecordShortcut(
            id = 0L,
            target = target.data,
        ).let {
            recordShortcutViewDataMapper.map(
                shortcut = it,
                typesMap = typesMap,
                tags = tags,
                isDarkTheme = isDarkTheme,
                isFiltered = false,
                isEnabled = false,
            )
        }.let {
            if (target.noSettingAction) it.copy(data = it.data.copy(name = "")) else it
        }

        return ChangeShortcutViewData(
            targetModes = viewDataMapper.mapTargetModes(
                selected = params.targetMode,
            ),
            shortcutPreview = shortcutPreview,
            recordTypePreview = ChangeShortcutViewData.RecordTypePreviewViewData(
                icon = type?.icon
                    ?.let(iconMapper::mapIcon)
                    ?: RecordTypeIcon.Image(R.drawable.unknown),
                color = type?.color
                    ?.let { colorMapper.mapToColorInt(it, isDarkTheme) }
                    ?: colorMapper.toUntrackedColor(isDarkTheme),
            ),
            recordTagsPreview = ChangeShortcutViewData.RecordTagsPreviewViewData(
                count = params.recordTags.size,
            ),
            actionPreview = viewDataMapper.mapSettingActionsTitle(
                selected = params.settingAction,
            ),
            showRecordTarget = params.targetMode == RecordShortcut.TargetMode.Record,
            showSettingTarget = params.targetMode == RecordShortcut.TargetMode.Setting,
        )
    }

    fun buildTarget(
        params: Params,
    ): BuildTargetResult {
        var noTypeId = false
        var noSettingAction = false

        val target = when (params.targetMode) {
            RecordShortcut.TargetMode.Record -> {
                noTypeId = params.recordTypeId.orZero() == 0L
                RecordShortcut.Target.Record(
                    typeId = params.recordTypeId.orZero(),
                    comment = params.comment,
                    tags = params.recordTags,
                )
            }
            RecordShortcut.TargetMode.Setting -> {
                noSettingAction = params.settingAction == null
                RecordShortcut.Target.Setting(
                    action = params.settingAction
                        ?: RecordShortcut.SettingAction.Toggle(QuickSettingsWidgetType.AllowMultitasking),
                )
            }
        }
        return BuildTargetResult(
            data = target,
            noTypeId = noTypeId,
            noSettingAction = noSettingAction,
        )
    }

    data class BuildTargetResult(
        val data: RecordShortcut.Target,
        val noTypeId: Boolean,
        val noSettingAction: Boolean,
    )

    data class Params(
        val targetMode: RecordShortcut.TargetMode,
        val recordTypeId: Long?,
        val recordTags: List<RecordBase.Tag>,
        val comment: String,
        val settingAction: RecordShortcut.SettingAction?,
    )
}
