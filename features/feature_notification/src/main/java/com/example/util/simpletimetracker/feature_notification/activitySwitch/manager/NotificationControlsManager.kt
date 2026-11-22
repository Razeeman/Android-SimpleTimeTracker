package com.example.util.simpletimetracker.feature_notification.activitySwitch.manager

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.view.ContextThemeWrapper
import android.view.View
import android.widget.RemoteViews
import androidx.annotation.DrawableRes
import androidx.core.net.toUri
import com.example.util.simpletimetracker.core.extension.allowVmViolations
import com.example.util.simpletimetracker.core.utils.PendingIntents
import com.example.util.simpletimetracker.domain.extension.ifNull
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.record.model.RecordBase
import com.example.util.simpletimetracker.feature_notification.R
import com.example.util.simpletimetracker.feature_notification.activitySwitch.mapper.NotificationControlsMapper
import com.example.util.simpletimetracker.feature_notification.core.TAG_VALUE_DECIMAL_DELIMITER
import com.example.util.simpletimetracker.feature_notification.core.TAG_VALUE_MINUS_SIGN
import com.example.util.simpletimetracker.feature_notification.recevier.NotificationReceiver
import com.example.util.simpletimetracker.feature_notification.recordType.customView.NotificationIconView
import com.example.util.simpletimetracker.feature_views.GoalCheckmarkView
import com.example.util.simpletimetracker.feature_views.extension.getBitmapFromView
import com.example.util.simpletimetracker.feature_views.extension.measureExactly
import com.example.util.simpletimetracker.feature_views.viewData.ColorStyle
import com.example.util.simpletimetracker.feature_views.viewData.RecordTypeIcon
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class NotificationControlsManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notificationControlsMapper: NotificationControlsMapper,
) {

    private val iconView = allowVmViolations {
        NotificationIconView(ContextThemeWrapper(context, R.style.AppTheme))
    }
    private val checkView = allowVmViolations {
        GoalCheckmarkView(ContextThemeWrapper(context, R.style.AppTheme))
    }
    private val iconSize by lazy {
        context.resources.getDimensionPixelSize(R.dimen.notification_icon_size)
    }
    private val checkSize by lazy {
        context.resources.getDimensionPixelSize(R.dimen.notification_icon_half_size)
    }

    fun getControlsView(
        from: From,
        controls: NotificationControlsParams,
        isBig: Boolean,
    ): RemoteViews? {
        if (controls !is NotificationControlsParams.Enabled) return null
        if (!isBig) return null

        return RemoteViews(context.packageName, R.layout.notification_switch_controls_layout).apply {
            val hintVisibility = View.VISIBLE
            setViewVisibility(R.id.tvNotificationControlsHint, hintVisibility)
            val fullHint = controls.viewState.hint
            setTextViewText(R.id.tvNotificationControlsHint, fullHint)

            disableRootClick()

            when (controls.viewState) {
                is NotificationControlsParams.ViewState.TypeSelection -> {
                    val shortcutsControlsVisible: Boolean = controls.viewState.shortcuts.isNotEmpty() &&
                        controls.shortcutsShift != null
                    val tagsControlsVisible: Boolean = controls.viewState.tags.isNotEmpty() &&
                        !shortcutsControlsVisible

                    addTypeControls(from, controls, controls.viewState)
                    when {
                        shortcutsControlsVisible -> addShortcutControls(from, controls, controls.viewState)
                        tagsControlsVisible -> addTagControls(from, controls, controls.viewState)
                    }

                    setViewVisibility(R.id.containerNotificationTypesPrev, View.VISIBLE)
                    setViewVisibility(R.id.containerNotificationTypesNext, View.VISIBLE)
                    val tagsControlsVisibility = if (tagsControlsVisible || shortcutsControlsVisible) {
                        View.VISIBLE
                    } else {
                        View.GONE
                    }
                    setViewVisibility(R.id.containerNotificationTags, tagsControlsVisibility)
                    setViewVisibility(R.id.containerNotificationTagsPrev, tagsControlsVisibility)
                    setViewVisibility(R.id.containerNotificationTagsNext, tagsControlsVisibility)
                }
                is NotificationControlsParams.ViewState.TagValueSelection -> {
                    // TODO TAG show suggestions?
                    addTagValueSelectionControls(from, controls, controls.viewState)

                    setViewVisibility(R.id.containerNotificationTypesPrev, View.VISIBLE)
                    setViewVisibility(R.id.containerNotificationTypesNext, View.VISIBLE)
                    setViewVisibility(R.id.containerNotificationTags, View.VISIBLE)
                    setViewVisibility(R.id.containerNotificationTagsPrev, View.INVISIBLE)
                    setViewVisibility(R.id.containerNotificationTagsNext, View.VISIBLE)
                }
            }
        }
    }

    private fun RemoteViews.disableRootClick() {
        // Do nothing on click.
        setOnClickPendingIntent(
            R.id.containerNotificationControlsRoot,
            PendingIntent.getBroadcast(
                context,
                0,
                Intent(context, NotificationReceiver::class.java),
                PendingIntents.getFlags(),
            ),
        )
    }

    private fun RemoteViews.addTypeControls(
        from: From,
        params: NotificationControlsParams.Enabled,
        viewState: NotificationControlsParams.ViewState.TypeSelection,
    ) {
        // Prev button
        setImageViewBitmap(
            R.id.ivNotificationTypesPrev,
            getIconBitmap(viewState.controlIconPrev, params.controlIconColor),
        )
        setOnClickPendingIntent(
            R.id.btnNotificationTypesPrev,
            getPendingSelfIntent(
                context = context,
                action = ACTION_NOTIFICATION_CONTROLS_TYPES_PREV,
                requestCode = getRequestCode(from),
                from = from,
                selectedTags = params.selectedTags,
                editingTagId = params.editingTagId,
                editingTagValueInput = params.editingTagValueInput,
                recordTypesShift = (params.typesShift - TYPES_LIST_SIZE)
                    .takeUnless { it < 0 }
                    .ifNull { viewState.types.size - TYPES_LIST_SIZE }
                    .coerceAtLeast(0),
                recordTagsShift = params.tagsShift,
                shortcutsShift = params.shortcutsShift,
                isMultipleTagAvailable = params.isMultipleTagAvailable,
                requiredValueSelectionTagIds = params.requiredValueSelectionTagIds,
            ),
        )

        // Types buttons
        val currentTypes = viewState.types.drop(params.typesShift).take(TYPES_LIST_SIZE)

        fun addPresentType(data: NotificationControlsParams.Type.Present) {
            val recordTypeId = (from as? From.ActivityNotification)?.recordTypeId
            val action = when (data.action) {
                is NotificationControlsParams.Type.Action.Repeat -> {
                    ACTION_NOTIFICATION_CONTROLS_REPEAT
                }
                is NotificationControlsParams.Type.Action.Shortcuts -> {
                    ACTION_NOTIFICATION_CONTROLS_SHORTCUTS_TOGGLE
                }
                is NotificationControlsParams.Type.Action.Select -> {
                    when (from) {
                        is From.ActivityNotification -> {
                            if (data.action.typeId == recordTypeId) {
                                ACTION_NOTIFICATION_CONTROLS_STOP
                            } else {
                                ACTION_NOTIFICATION_CONTROLS_TYPE_CLICK
                            }
                        }
                        is From.ActivitySwitch -> {
                            ACTION_NOTIFICATION_CONTROLS_TYPE_CLICK
                        }
                    }
                }
            }
            val selectedTypeId = (data.action as? NotificationControlsParams.Type.Action.Select)?.typeId
            val isShortcuts = data.action is NotificationControlsParams.Type.Action.Shortcuts
            val color = if (
                data.action is NotificationControlsParams.Type.Action.Select &&
                recordTypeId == selectedTypeId
            ) {
                ColorStyle.Solid(viewState.filteredTypeColor)
            } else {
                data.color
            }
            getTypeControlView(
                icon = data.icon,
                color = color,
                checkStates = data.checkStates,
                isComplete = data.isComplete,
                intent = getPendingSelfIntent(
                    context = context,
                    action = action,
                    requestCode = getRequestCode(
                        from = from,
                        additionalInfo = when (data.action) {
                            is NotificationControlsParams.Type.Action.Repeat -> {
                                RequestCode.AdditionalInfo.Repeat
                            }
                            is NotificationControlsParams.Type.Action.Shortcuts -> {
                                RequestCode.AdditionalInfo.Shortcuts
                            }
                            is NotificationControlsParams.Type.Action.Select -> {
                                RequestCode.AdditionalInfo.TypeId(data.action.typeId)
                            }
                        },
                    ),
                    from = from,
                    selectedTags = if (isShortcuts) emptyList() else params.selectedTags,
                    editingTagId = if (isShortcuts) null else params.editingTagId,
                    editingTagValueInput = if (isShortcuts) null else params.editingTagValueInput,
                    recordTypesShift = params.typesShift,
                    recordTagsShift = if (isShortcuts) 0 else params.tagsShift,
                    shortcutsShift = if (isShortcuts) params.shortcutsShift else null,
                    selectedTypeId = if (isShortcuts) 0L else selectedTypeId,
                    isMultipleTagAvailable = !isShortcuts && params.isMultipleTagAvailable,
                    requiredValueSelectionTagIds = if (isShortcuts) {
                        emptyList()
                    } else {
                        params.requiredValueSelectionTagIds
                    },
                ),
            ).let {
                addView(R.id.containerNotificationTypes, it)
            }
        }

        // Populate container with empty items to preserve prev next controls position
        fun addEmptyType() {
            getTypeControlView(
                icon = null,
                color = null,
                checkStates = emptyList(),
                isComplete = false,
                intent = null,
            ).let {
                addView(R.id.containerNotificationTypes, it)
            }
        }

        currentTypes.forEach {
            when (it) {
                is NotificationControlsParams.Type.Present -> addPresentType(it)
                is NotificationControlsParams.Type.Empty -> addEmptyType()
            }
        }

        // Next button
        setImageViewBitmap(
            R.id.ivNotificationTypesNext,
            getIconBitmap(viewState.controlIconNext, params.controlIconColor),
        )
        setOnClickPendingIntent(
            R.id.btnNotificationTypesNext,
            getPendingSelfIntent(
                context = context,
                action = ACTION_NOTIFICATION_CONTROLS_TYPES_NEXT,
                requestCode = getRequestCode(from),
                from = from,
                selectedTags = params.selectedTags,
                editingTagId = params.editingTagId,
                editingTagValueInput = params.editingTagValueInput,
                recordTypesShift = (params.typesShift + TYPES_LIST_SIZE)
                    .takeUnless { it >= viewState.types.size }
                    .orZero(),
                recordTagsShift = params.tagsShift,
                shortcutsShift = params.shortcutsShift,
                isMultipleTagAvailable = params.isMultipleTagAvailable,
                requiredValueSelectionTagIds = params.requiredValueSelectionTagIds,
            ),
        )
    }

    private fun RemoteViews.addShortcutControls(
        from: From,
        params: NotificationControlsParams.Enabled,
        viewState: NotificationControlsParams.ViewState.TypeSelection,
    ) {
        val index = params.shortcutsShift ?: return
        val count = viewState.shortcuts.size
        val shortcut = viewState.shortcuts.getOrNull(index) ?: return

        fun getShortcutIntent(
            action: String,
            shift: Int,
            shortcutId: Long? = null,
        ): PendingIntent {
            return getPendingSelfIntent(
                context = context,
                action = action,
                requestCode = getRequestCode(
                    from = from,
                    additionalInfo = shortcutId
                        ?.let { RequestCode.AdditionalInfo.ShortcutId(it) }
                        ?: RequestCode.AdditionalInfo.Shortcuts,
                ),
                from = from,
                selectedTypeId = 0L,
                recordTypesShift = params.typesShift,
                recordTagsShift = 0,
                shortcutsShift = shift,
                shortcutId = shortcutId,
                isMultipleTagAvailable = false,
            )
        }

        // Prev button
        setImageViewBitmap(
            R.id.ivNotificationTagsPrev,
            getIconBitmap(viewState.controlIconPrev, params.controlIconColor),
        )
        setOnClickPendingIntent(
            R.id.btnNotificationTagsPrev,
            getShortcutIntent(
                action = ACTION_NOTIFICATION_CONTROLS_SHORTCUTS_PREV,
                shift = (index - 1 + count) % count,
            ),
        )

        // Shortcut button
        addView(
            R.id.containerNotificationTags,
            getTagControlView(
                text = shortcut.text,
                color = shortcut.color,
                iconResId = shortcut.iconResId,
                intent = getShortcutIntent(
                    action = ACTION_NOTIFICATION_CONTROLS_SHORTCUT_CLICK,
                    shift = index,
                    shortcutId = shortcut.id,
                ),
                isSelected = false,
            ),
        )

        // Next button
        setImageViewBitmap(
            R.id.ivNotificationTagsNext,
            getIconBitmap(viewState.controlIconNext, params.controlIconColor),
        )
        setOnClickPendingIntent(
            R.id.btnNotificationTagsNext,
            getShortcutIntent(
                action = ACTION_NOTIFICATION_CONTROLS_SHORTCUTS_NEXT,
                shift = (index + 1) % count,
            ),
        )
    }

    private fun RemoteViews.addTagControls(
        from: From,
        params: NotificationControlsParams.Enabled,
        viewState: NotificationControlsParams.ViewState.TypeSelection,
    ) {
        // Prev button
        setImageViewBitmap(
            R.id.ivNotificationTagsPrev,
            getIconBitmap(viewState.controlIconPrev, params.controlIconColor),
        )
        setOnClickPendingIntent(
            R.id.btnNotificationTagsPrev,
            getPendingSelfIntent(
                context = context,
                action = ACTION_NOTIFICATION_CONTROLS_TAGS_PREV,
                requestCode = getRequestCode(from),
                from = from,
                selectedTypeId = params.selectedTypeId,
                selectedTags = params.selectedTags,
                editingTagId = params.editingTagId,
                editingTagValueInput = params.editingTagValueInput,
                recordTypesShift = params.typesShift,
                recordTagsShift = (params.tagsShift - TAGS_LIST_SIZE)
                    .takeUnless { it < 0 }
                    .ifNull { viewState.tags.size - TAGS_LIST_SIZE }
                    .coerceAtLeast(0),
                isMultipleTagAvailable = params.isMultipleTagAvailable,
                requiredValueSelectionTagIds = params.requiredValueSelectionTagIds,
            ),
        )

        // Tags buttons
        val currentTags = viewState.tags.drop(params.tagsShift).take(TAGS_LIST_SIZE)

        fun addPresentType(data: NotificationControlsParams.Tag.Present) {
            val action = when (data.action) {
                is NotificationControlsParams.Tag.Action.Select -> ACTION_NOTIFICATION_CONTROLS_TAG_CLICK
                is NotificationControlsParams.Tag.Action.Apply -> ACTION_NOTIFICATION_CONTROLS_APPLY_TAGS
                is NotificationControlsParams.Tag.Action.Clear -> ACTION_NOTIFICATION_CONTROLS_CLEAR_TAGS
            }
            getTagControlView(
                text = data.text,
                color = data.color,
                intent = getPendingSelfIntent(
                    context = context,
                    action = action,
                    requestCode = getRequestCode(
                        from = from,
                        additionalInfo = when (data.action) {
                            is NotificationControlsParams.Tag.Action.Select -> {
                                RequestCode.AdditionalInfo.TagId(data.action.tagId)
                            }
                            is NotificationControlsParams.Tag.Action.Apply -> {
                                RequestCode.AdditionalInfo.ApplyTags
                            }
                            is NotificationControlsParams.Tag.Action.Clear -> {
                                RequestCode.AdditionalInfo.ClearTags
                            }
                        },
                    ),
                    from = from,
                    selectedTypeId = params.selectedTypeId,
                    selectedTags = params.selectedTags,
                    editingTagId = params.editingTagId,
                    editingTagValueInput = params.editingTagValueInput,
                    recordTypesShift = params.typesShift,
                    recordTagsShift = params.tagsShift,
                    tagId = (data.action as? NotificationControlsParams.Tag.Action.Select)?.tagId,
                    isMultipleTagAvailable = params.isMultipleTagAvailable,
                    requiredValueSelectionTagIds = params.requiredValueSelectionTagIds,
                ),
                isSelected = data.isSelected,
            ).let {
                addView(R.id.containerNotificationTags, it)
            }
        }

        // Populate container with empty items to preserve prev next controls position
        fun addEmptyType() {
            getTagControlView(
                text = "",
                color = null,
                intent = null,
            ).let {
                addView(R.id.containerNotificationTags, it)
            }
        }

        currentTags.forEach {
            when (it) {
                is NotificationControlsParams.Tag.Present -> addPresentType(it)
                is NotificationControlsParams.Tag.Empty -> addEmptyType()
            }
        }

        // Next button
        setImageViewBitmap(
            R.id.ivNotificationTagsNext,
            getIconBitmap(viewState.controlIconNext, params.controlIconColor),
        )
        setOnClickPendingIntent(
            R.id.btnNotificationTagsNext,
            getPendingSelfIntent(
                context = context,
                action = ACTION_NOTIFICATION_CONTROLS_TAGS_NEXT,
                requestCode = getRequestCode(from),
                from = from,
                selectedTypeId = params.selectedTypeId,
                selectedTags = params.selectedTags,
                editingTagId = params.editingTagId,
                editingTagValueInput = params.editingTagValueInput,
                recordTypesShift = params.typesShift,
                recordTagsShift = (params.tagsShift + TAGS_LIST_SIZE)
                    .takeUnless { it >= viewState.tags.size }
                    .orZero(),
                isMultipleTagAvailable = params.isMultipleTagAvailable,
                requiredValueSelectionTagIds = params.requiredValueSelectionTagIds,
            ),
        )
    }

    private fun RemoteViews.addTagValueSelectionControls(
        from: From,
        params: NotificationControlsParams.Enabled,
        viewState: NotificationControlsParams.ViewState.TagValueSelection,
    ) {
        // Back button
        setImageViewBitmap(
            R.id.ivNotificationTypesPrev,
            getIconBitmap(viewState.controlIconBack, viewState.controlBackColor),
        )
        setOnClickPendingIntent(
            R.id.btnNotificationTypesPrev,
            getPendingSelfIntent(
                context = context,
                action = ACTION_NOTIFICATION_CONTROLS_TAG_VALUE_BACK,
                requestCode = getRequestCode(from),
                from = from,
                selectedTypeId = params.selectedTypeId,
                selectedTags = params.selectedTags,
                recordTypesShift = params.typesShift,
                recordTagsShift = params.tagsShift,
                isMultipleTagAvailable = params.isMultipleTagAvailable,
                requiredValueSelectionTagIds = params.requiredValueSelectionTagIds,
            ),
        )

        // Control buttons
        fun addPresentType(
            index: Long,
            containerId: Int,
            data: NotificationControlsParams.TagValueControls.Present,
        ) {
            val currentValue = params.editingTagValueInput.orEmpty()
            val newTagValueOnThisClick = when (data.type) {
                is NotificationControlsParams.TagValueControls.Present.Type.Number -> {
                    currentValue.plus(data.type.number.toString())
                }
                NotificationControlsParams.TagValueControls.Present.Type.PlusMinus -> {
                    if (currentValue.startsWith(TAG_VALUE_MINUS_SIGN)) {
                        currentValue.removePrefix(TAG_VALUE_MINUS_SIGN.toString())
                    } else {
                        TAG_VALUE_MINUS_SIGN + currentValue
                    }
                }
                NotificationControlsParams.TagValueControls.Present.Type.Dot -> {
                    currentValue.plus(TAG_VALUE_DECIMAL_DELIMITER)
                }
            }
            getTagControlView(
                text = data.text,
                color = data.color,
                intent = getPendingSelfIntent(
                    context = context,
                    action = ACTION_NOTIFICATION_CONTROLS_TAG_VALUE_UPDATE,
                    requestCode = getRequestCode(
                        from = from,
                        additionalInfo = RequestCode.AdditionalInfo.TagValueControls(index),
                    ),
                    from = from,
                    selectedTypeId = params.selectedTypeId,
                    selectedTags = params.selectedTags,
                    editingTagId = params.editingTagId,
                    editingTagValueInput = newTagValueOnThisClick,
                    recordTypesShift = params.typesShift,
                    recordTagsShift = params.tagsShift,
                    isMultipleTagAvailable = params.isMultipleTagAvailable,
                    requiredValueSelectionTagIds = params.requiredValueSelectionTagIds,
                ),
            ).let {
                addView(containerId, it)
            }
        }

        // Populate container with empty items to preserve prev next controls position
        fun addEmptyType(
            containerId: Int,
        ) {
            getTagControlView(
                text = "",
                color = null,
                intent = null,
            ).let {
                addView(containerId, it)
            }
        }

        viewState.numbers.forEachIndexed { index, number ->
            if (index < 6) {
                val data = number as? NotificationControlsParams.TagValueControls.Present
                    ?: return@forEachIndexed
                addPresentType(
                    index = index.toLong(),
                    containerId = R.id.containerNotificationTypes,
                    data = data,
                )
            } else {
                val containerId = R.id.containerNotificationTags
                when (number) {
                    is NotificationControlsParams.TagValueControls.Present -> {
                        addPresentType(
                            index = index.toLong(),
                            containerId = containerId,
                            data = number,
                        )
                    }
                    is NotificationControlsParams.TagValueControls.Empty -> {
                        addEmptyType(containerId)
                    }
                }
            }
        }

        // Save button
        setImageViewBitmap(
            R.id.ivNotificationTypesNext,
            getIconBitmap(viewState.controlIconSave, viewState.controlSaveColor),
        )
        setOnClickPendingIntent(
            R.id.btnNotificationTypesNext,
            getPendingSelfIntent(
                context = context,
                action = ACTION_NOTIFICATION_CONTROLS_TAG_VALUE_SAVE,
                requestCode = getRequestCode(from),
                from = from,
                selectedTypeId = params.selectedTypeId,
                selectedTags = params.selectedTags,
                editingTagId = params.editingTagId,
                editingTagValueInput = params.editingTagValueInput,
                recordTypesShift = params.typesShift,
                recordTagsShift = params.tagsShift,
                isMultipleTagAvailable = params.isMultipleTagAvailable,
                requiredValueSelectionTagIds = params.requiredValueSelectionTagIds,
            ),
        )

        // Remove button
        setImageViewBitmap(
            R.id.ivNotificationTagsNext,
            getIconBitmap(viewState.controlIconRemove, params.controlIconColor),
        )
        setOnClickPendingIntent(
            R.id.btnNotificationTagsNext,
            getPendingSelfIntent(
                context = context,
                action = ACTION_NOTIFICATION_CONTROLS_TAG_VALUE_REMOVE,
                requestCode = getRequestCode(from),
                from = from,
                selectedTypeId = params.selectedTypeId,
                selectedTags = params.selectedTags,
                editingTagId = params.editingTagId,
                editingTagValueInput = params.editingTagValueInput?.dropLast(1)
                    .takeUnless { it.isNullOrEmpty() },
                recordTypesShift = params.typesShift,
                recordTagsShift = params.tagsShift,
                isMultipleTagAvailable = params.isMultipleTagAvailable,
                requiredValueSelectionTagIds = params.requiredValueSelectionTagIds,
            ),
        )
    }

    private fun getTypeControlView(
        icon: RecordTypeIcon?,
        color: ColorStyle?,
        checkStates: List<GoalCheckmarkView.CheckState>,
        isComplete: Boolean,
        intent: PendingIntent?,
    ): RemoteViews {
        return RemoteViews(context.packageName, R.layout.notification_type_layout)
            .apply {
                if (icon != null && color != null) {
                    val bitmap = getIconBitmap(
                        icon = icon,
                        color = color,
                        checkStates = checkStates,
                        isComplete = isComplete,
                    )
                    setViewVisibility(R.id.containerNotificationType, View.VISIBLE)
                    setImageViewBitmap(R.id.ivNotificationType, bitmap)
                } else {
                    setViewVisibility(R.id.containerNotificationType, View.INVISIBLE)
                }
                if (intent != null) {
                    setOnClickPendingIntent(R.id.btnNotificationType, intent)
                }
            }
    }

    private fun getTagControlView(
        text: String,
        color: Int?,
        intent: PendingIntent?,
        isSelected: Boolean = false,
        @DrawableRes iconResId: Int? = null,
    ): RemoteViews {
        val checkBitmap by lazy {
            synchronized(checkView) {
                checkView.apply {
                    itemCheckState = GoalCheckmarkView.CheckState.GOAL_REACHED
                    measureExactly(checkSize)
                }.getBitmapFromView()
            }
        }
        return RemoteViews(context.packageName, R.layout.notification_tag_layout)
            .apply {
                setTextViewText(R.id.tvNotificationTag, text)
                if (iconResId != null) {
                    setViewVisibility(R.id.ivNotificationShortcutIcon, View.VISIBLE)
                    setImageViewResource(R.id.ivNotificationShortcutIcon, iconResId)
                } else {
                    setViewVisibility(R.id.ivNotificationShortcutIcon, View.GONE)
                }
                if (color != null) {
                    setViewVisibility(R.id.containerNotificationTag, View.VISIBLE)
                    setInt(R.id.ivNotificationTag, "setColorFilter", color)
                } else {
                    setViewVisibility(R.id.containerNotificationTag, View.INVISIBLE)
                }
                if (isSelected) {
                    setViewVisibility(R.id.ivNotificationTagSelectionCheck, View.VISIBLE)
                    setImageViewBitmap(R.id.ivNotificationTagSelectionCheck, checkBitmap)
                } else {
                    setViewVisibility(R.id.ivNotificationTagSelectionCheck, View.GONE)
                }
                if (intent != null) {
                    setOnClickPendingIntent(R.id.btnNotificationTag, intent)
                }
            }
    }

    private fun getPendingSelfIntent(
        context: Context,
        action: String,
        requestCode: Int,
        from: From,
        selectedTypeId: Long? = null,
        selectedTags: List<RecordBase.Tag> = emptyList(),
        editingTagId: Long? = null,
        editingTagValueInput: String? = null,
        recordTypesShift: Int? = null,
        recordTagsShift: Int? = null,
        shortcutsShift: Int? = null,
        shortcutId: Long? = null,
        tagId: Long? = null,
        isMultipleTagAvailable: Boolean,
        requiredValueSelectionTagIds: List<Long> = emptyList(),
    ): PendingIntent {
        val intent = Intent(context, NotificationReceiver::class.java)
        intent.action = action
        shortcutId?.let {
            val source = when (from) {
                is From.ActivityNotification -> "activity-notification/${from.recordTypeId}"
                is From.ActivitySwitch -> "activity-switch"
            }
            // Keep shortcut identities distinct even when request code hashes collide.
            intent.data = "simpletimetracker://notification-shortcut/$source/$it".toUri()
        }
        notificationControlsMapper.mapFromToExtra(from).let { intent.putExtra(ARGS_CONTROLS_FROM, it) }
        (from as? From.ActivityNotification)?.recordTypeId?.let { intent.putExtra(ARGS_TYPE_ID, it) }
        selectedTypeId?.let { intent.putExtra(ARGS_SELECTED_TYPE_ID, it) }
        intent.putExtra(ARGS_SELECTED_TAGS, serializeSelectedTags(selectedTags))
        editingTagId?.let { intent.putExtra(ARGS_EDITING_TAG_ID, it) }
        editingTagValueInput?.let { intent.putExtra(ARGS_EDITING_TAG_VALUE_INPUT, it) }
        tagId?.let { intent.putExtra(ARGS_CLICKED_TAG_ID, it) }
        recordTypesShift.let { intent.putExtra(ARGS_TYPES_SHIFT, it) }
        recordTagsShift?.let { intent.putExtra(ARGS_TAGS_SHIFT, it) }
        shortcutsShift?.let { intent.putExtra(ARGS_SHORTCUTS_SHIFT, it) }
        shortcutId?.let { intent.putExtra(ARGS_CLICKED_SHORTCUT_ID, it) }
        intent.putExtra(ARGS_REQUIRED_VALUE_SELECTION_TAGS, requiredValueSelectionTagIds.toLongArray())
        intent.putExtra(ARGS_MULTIPLE_TAG_AVAILABLE, isMultipleTagAvailable)
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntents.getFlags(),
        )
    }

    private fun serializeSelectedTags(tags: List<RecordBase.Tag>): String {
        if (tags.isEmpty()) return ""
        return tags.joinToString(separator = ";") { tag ->
            val valueText = tag.numericValue?.let(Double::toString).orEmpty()
            "${tag.tagId}=$valueText"
        }
    }

    private fun getIconBitmap(
        icon: RecordTypeIcon,
        color: Int,
    ): Bitmap {
        return getIconBitmap(
            icon = icon,
            color = ColorStyle.Solid(color),
        )
    }

    private fun getIconBitmap(
        icon: RecordTypeIcon,
        color: ColorStyle,
        checkStates: List<GoalCheckmarkView.CheckState> = emptyList(),
        isComplete: Boolean = false,
    ): Bitmap = synchronized(iconView) {
        return iconView.apply {
            itemIcon = icon
            itemColor = color
            itemCheckStates = checkStates
            itemIsComplete = isComplete
            measureExactly(iconSize)
        }.getBitmapFromView()
    }

    // TODO Use collision-free Intent.data identities for the remaining controls, including source and target.
    // additionalInfo is to make code unique.
    private fun getRequestCode(
        from: From,
        additionalInfo: RequestCode.AdditionalInfo = RequestCode.AdditionalInfo.Nothing,
    ): Int {
        return RequestCode(
            from = from,
            additionalInfo = additionalInfo,
        ).hashCode()
    }

    sealed interface From {
        data class ActivityNotification(val recordTypeId: Long) : From
        data object ActivitySwitch : From
    }

    private data class RequestCode(
        val from: From,
        val additionalInfo: AdditionalInfo,
    ) {

        sealed interface AdditionalInfo {
            data class TypeId(val id: Long) : AdditionalInfo
            data class TagId(val id: Long) : AdditionalInfo
            data class ShortcutId(val id: Long) : AdditionalInfo
            data class TagValueControls(val id: Long) : AdditionalInfo
            data object Repeat : AdditionalInfo
            data object Shortcuts : AdditionalInfo
            data object ApplyTags : AdditionalInfo
            data object ClearTags : AdditionalInfo
            data object Nothing : AdditionalInfo
        }
    }

    companion object {
        const val ACTION_NOTIFICATION_CONTROLS_STOP =
            "com.example.util.simpletimetracker.feature_notification.activitySwitch.onStop"
        const val ACTION_NOTIFICATION_CONTROLS_TYPE_CLICK =
            "com.example.util.simpletimetracker.feature_notification.activitySwitch.onTypeClick"
        const val ACTION_NOTIFICATION_CONTROLS_REPEAT =
            "com.example.util.simpletimetracker.feature_notification.activitySwitch.onRepeat"
        const val ACTION_NOTIFICATION_CONTROLS_SHORTCUTS_TOGGLE =
            "com.example.util.simpletimetracker.feature_notification.activitySwitch.onShortcutsToggle"
        const val ACTION_NOTIFICATION_CONTROLS_SHORTCUTS_PREV =
            "com.example.util.simpletimetracker.feature_notification.activitySwitch.onShortcutsPrevClick"
        const val ACTION_NOTIFICATION_CONTROLS_SHORTCUTS_NEXT =
            "com.example.util.simpletimetracker.feature_notification.activitySwitch.onShortcutsNextClick"
        const val ACTION_NOTIFICATION_CONTROLS_SHORTCUT_CLICK =
            "com.example.util.simpletimetracker.feature_notification.activitySwitch.onShortcutClick"
        const val ACTION_NOTIFICATION_CONTROLS_TAG_CLICK =
            "com.example.util.simpletimetracker.feature_notification.activitySwitch.onTagClick"
        const val ACTION_NOTIFICATION_CONTROLS_APPLY_TAGS =
            "com.example.util.simpletimetracker.feature_notification.activitySwitch.onApplyTags"
        const val ACTION_NOTIFICATION_CONTROLS_CLEAR_TAGS =
            "com.example.util.simpletimetracker.feature_notification.activitySwitch.onClearTags"

        const val ACTION_NOTIFICATION_CONTROLS_TYPES_PREV =
            "com.example.util.simpletimetracker.feature_notification.activitySwitch.onTypesPrevClick"
        const val ACTION_NOTIFICATION_CONTROLS_TYPES_NEXT =
            "com.example.util.simpletimetracker.feature_notification.activitySwitch.onTypesNextClick"
        const val ACTION_NOTIFICATION_CONTROLS_TAGS_PREV =
            "com.example.util.simpletimetracker.feature_notification.activitySwitch.onTagsPrevClick"
        const val ACTION_NOTIFICATION_CONTROLS_TAGS_NEXT =
            "com.example.util.simpletimetracker.feature_notification.activitySwitch.onTagsNextClick"
        const val ACTION_NOTIFICATION_CONTROLS_TAG_VALUE_BACK =
            "com.example.util.simpletimetracker.feature_notification.activitySwitch.onTagValueBack"
        const val ACTION_NOTIFICATION_CONTROLS_TAG_VALUE_SAVE =
            "com.example.util.simpletimetracker.feature_notification.activitySwitch.onTagValueSave"
        const val ACTION_NOTIFICATION_CONTROLS_TAG_VALUE_REMOVE =
            "com.example.util.simpletimetracker.feature_notification.activitySwitch.onTagValueRemove"
        const val ACTION_NOTIFICATION_CONTROLS_TAG_VALUE_UPDATE =
            "com.example.util.simpletimetracker.feature_notification.activitySwitch.onTagValueUpdate"

        const val ARGS_CONTROLS_FROM = "controlsFrom"
        const val ARGS_TYPE_ID = "typeId"
        const val ARGS_SELECTED_TYPE_ID = "selectedTypeId"
        const val ARGS_SELECTED_TAGS = "selectedTags"
        const val ARGS_EDITING_TAG_ID = "editingTagId"
        const val ARGS_EDITING_TAG_VALUE_INPUT = "editingTagValueInput"
        const val ARGS_REQUIRED_VALUE_SELECTION_TAGS = "requiredTagSelectionTags"
        const val ARGS_CLICKED_TAG_ID = "clickedTagId"
        const val ARGS_TYPES_SHIFT = "typesShift"
        const val ARGS_TAGS_SHIFT = "tagsShift"
        const val ARGS_SHORTCUTS_SHIFT = "shortcutsShift"
        const val ARGS_CLICKED_SHORTCUT_ID = "clickedShortcutId"
        const val ARGS_MULTIPLE_TAG_AVAILABLE = "multipleTagAvailable"

        const val TYPES_LIST_SIZE = 6
        const val TAGS_LIST_SIZE = 4
    }
}