package com.example.util.simpletimetracker.feature_widget.single

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RemoteViews
import com.example.util.simpletimetracker.core.extension.allowDiskRead
import com.example.util.simpletimetracker.core.extension.allowVmViolations
import com.example.util.simpletimetracker.core.interactor.CompleteTypesStateInteractor
import com.example.util.simpletimetracker.core.interactor.FilterGoalsByDayOfWeekInteractor
import com.example.util.simpletimetracker.core.interactor.GetCurrentRecordsDurationInteractor
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.IconMapper
import com.example.util.simpletimetracker.core.mapper.RecordTypeViewDataMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.core.utils.PendingIntents
import com.example.util.simpletimetracker.domain.base.REPEAT_BUTTON_ITEM_ID
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.record.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.record.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.domain.recordType.extension.getDaily
import com.example.util.simpletimetracker.domain.recordType.extension.getLongest
import com.example.util.simpletimetracker.domain.recordType.interactor.RecordTypeGoalInteractor
import com.example.util.simpletimetracker.domain.recordType.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.widget.interactor.WidgetInteractor
import com.example.util.simpletimetracker.feature_views.ColorUtils
import com.example.util.simpletimetracker.feature_views.GoalCheckmarkView
import com.example.util.simpletimetracker.feature_views.RecordTypeView
import com.example.util.simpletimetracker.feature_views.extension.dpToPx
import com.example.util.simpletimetracker.feature_views.extension.getBitmapFromView
import com.example.util.simpletimetracker.feature_views.extension.measureExactly
import com.example.util.simpletimetracker.feature_views.extension.pxToDp
import com.example.util.simpletimetracker.feature_views.extension.setAllMargins
import com.example.util.simpletimetracker.feature_views.viewData.RecordTypeIcon
import com.example.util.simpletimetracker.feature_widget.R
import com.example.util.simpletimetracker.feature_widget.common.WidgetTypeClickManager
import com.example.util.simpletimetracker.feature_widget.common.WidgetViewsHolder
import com.example.util.simpletimetracker.feature_widget.utils.setRecordTypeTimers
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class WidgetSingleProvider : AppWidgetProvider() {

    @Inject
    lateinit var widgetTypeClickManager: WidgetTypeClickManager

    @Inject
    lateinit var runningRecordInteractor: RunningRecordInteractor

    @Inject
    lateinit var recordInteractor: RecordInteractor

    @Inject
    lateinit var recordTypeInteractor: RecordTypeInteractor

    @Inject
    lateinit var recordTypeGoalInteractor: RecordTypeGoalInteractor

    @Inject
    lateinit var widgetInteractor: WidgetInteractor

    @Inject
    lateinit var recordTypeViewDataMapper: RecordTypeViewDataMapper

    @Inject
    lateinit var colorMapper: ColorMapper

    @Inject
    lateinit var iconMapper: IconMapper

    @Inject
    lateinit var resourceRepo: ResourceRepo

    @Inject
    lateinit var prefsInteractor: PrefsInteractor

    @Inject
    lateinit var getCurrentRecordsDurationInteractor: GetCurrentRecordsDurationInteractor

    @Inject
    lateinit var filterGoalsByDayOfWeekInteractor: FilterGoalsByDayOfWeekInteractor

    @Inject
    lateinit var completeTypesStateInteractor: CompleteTypesStateInteractor

    @Inject
    lateinit var widgetViewsHolder: WidgetViewsHolder

    private var typeIdsToUpdate: List<Long> = emptyList()
    private var preparedView: RecordTypeView? = null
    private var entireView: View? = null

    override fun onReceive(context: Context?, intent: Intent?) {
        typeIdsToUpdate = intent?.getLongArrayExtra(TYPE_IDS_EXTRA)?.toList().orEmpty()
        super.onReceive(context, intent)
        if (intent?.action == ON_CLICK_ACTION) onClick(context, intent)
    }

    override fun onUpdate(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetIds: IntArray?,
    ) {
        appWidgetIds?.forEach { widgetId ->
            updateAppWidget(context, appWidgetManager, widgetId)
        }
    }

    override fun onDeleted(context: Context?, appWidgetIds: IntArray?) {
        allowDiskRead { MainScope() }.launch {
            appWidgetIds?.forEach { prefsInteractor.removeWidget(it) }
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetId: Int,
        newOptions: Bundle?,
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        updateAppWidget(context, appWidgetManager, appWidgetId)
    }

    private fun updateAppWidget(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetId: Int,
    ) {
        if (context == null || appWidgetManager == null) return

        allowDiskRead { MainScope() }.launch {
            val view: View
            val recordTypeId = prefsInteractor.getWidget(appWidgetId)
            val backgroundTransparency = prefsInteractor.getWidgetBackgroundTransparencyPercent()
            val retroactiveTrackingModeEnabled = prefsInteractor.getRetroactiveTrackingMode()
            val typeIds = typeIdsToUpdate
            if (typeIds.isNotEmpty() && recordTypeId !in typeIds) return@launch
            val runningRecord = if (runningRecordInteractor.has(recordTypeId)) {
                runningRecordInteractor.get(recordTypeId)
            } else {
                null
            }
            val prevRecord = if (retroactiveTrackingModeEnabled) {
                recordInteractor.getAllPrev(timeStarted = System.currentTimeMillis())
                    .firstOrNull { it.typeId == recordTypeId }
            } else {
                null
            }
            val isDarkTheme: Boolean = prefsInteractor.getDarkMode()

            if (recordTypeId == REPEAT_BUTTON_ITEM_ID) {
                val viewData = recordTypeViewDataMapper.mapToRepeatItem(
                    numberOfCards = 0, // Size is not used here.
                    isDarkTheme = isDarkTheme,
                )
                view = prepareView(
                    context = context,
                    recordTypeIcon = viewData.iconId,
                    recordTypeName = viewData.name,
                    recordTypeColor = viewData.color,
                    isColored = false,
                    checkState = GoalCheckmarkView.CheckState.HIDDEN,
                    isComplete = false,
                    backgroundTransparency = backgroundTransparency,
                )
            } else {
                val recordType = recordTypeInteractor.get(recordTypeId)
                val checkState = if (recordType != null) {
                    val goals = filterGoalsByDayOfWeekInteractor
                        .execute(recordTypeGoalInteractor.getByType(recordTypeId))
                    val dailyGoals = goals.getDaily()
                    val dailyCurrent = if (dailyGoals.isNotEmpty()) {
                        getCurrentRecordsDurationInteractor.getDailyCurrent(
                            typeId = recordTypeId,
                            runningRecord = runningRecord,
                        )
                    } else {
                        null
                    }
                    recordTypeViewDataMapper.mapGoalCheckmark(
                        goal = dailyGoals.getLongest(),
                        dailyCurrent = dailyCurrent,
                    )
                } else {
                    GoalCheckmarkView.CheckState.HIDDEN
                }
                val isColored = when {
                    runningRecord != null -> recordType != null
                    prevRecord != null -> true
                    else -> false
                }
                view = prepareView(
                    context = context,
                    recordTypeIcon = recordType?.icon
                        ?.let(iconMapper::mapIcon),
                    recordTypeName = recordType?.name,
                    recordTypeColor = recordType?.color
                        ?.let { colorMapper.mapToColorInt(it, isDarkTheme) },
                    isColored = isColored,
                    checkState = checkState,
                    isComplete = recordTypeId in completeTypesStateInteractor.widgetTypeIds,
                    backgroundTransparency = backgroundTransparency,
                )
            }

            val options = appWidgetManager.getAppWidgetOptions(appWidgetId)
            measureView(context, options, view)
            val bitmap = view.getBitmapFromView()

            val views = RemoteViews(context.packageName, R.layout.widget_layout)
            setRecordTypeTimers(runningRecord, prevRecord, views)
            views.setImageViewBitmap(R.id.ivWidgetBackground, bitmap)
            views.setOnClickPendingIntent(R.id.btnWidget, getPendingSelfIntent(context, appWidgetId))

            runCatching {
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }
    }

    private fun prepareView(
        context: Context,
        recordTypeIcon: RecordTypeIcon?,
        recordTypeName: String?,
        recordTypeColor: Int?,
        isColored: Boolean,
        checkState: GoalCheckmarkView.CheckState,
        isComplete: Boolean,
        backgroundTransparency: Long,
    ): View {
        val icon = recordTypeIcon
            ?: RecordTypeIcon.Image(R.drawable.unknown)

        val name = recordTypeName
            ?: R.string.widget_load_error.let(resourceRepo::getString)

        val textColor = if (isColored) {
            resourceRepo.getColor(R.color.colorIcon)
        } else {
            resourceRepo.getColor(R.color.widget_universal_empty_color)
        }

        val color = if (isColored && recordTypeColor != null) {
            recordTypeColor
        } else {
            ColorUtils.changeAlpha(
                color = resourceRepo.getColor(R.color.widget_universal_background_color),
                alpha = 1f - backgroundTransparency / 100f,
            )
        }

        val view = getView(context).apply {
            (parent as? ViewGroup)?.removeAllViews()
            itemIcon = icon
            itemName = name
            itemIconColor = textColor
            itemColor = color
            itemCheckState = checkState
            itemCompleteIsAnimated = false
            itemIsComplete = isComplete
        }

        return view
    }

    private fun getView(context: Context): RecordTypeView {
        preparedView?.let { return it }

        val view = widgetViewsHolder.getRecordTypeView(context).apply {
            getContainer().radius =
                resources.getDimensionPixelOffset(R.dimen.widget_universal_corner_radius).toFloat()
            getContainer().cardElevation = 0f
            getContainer().useCompatPadding = false
            getCheckmarkOutline().setAllMargins(4)
        }
        preparedView = view

        return view
    }

    private fun measureView(
        context: Context,
        options: Bundle,
        view: View,
    ) {
        val defaultWidth = context.resources.getDimensionPixelSize(R.dimen.record_type_card_width)
        val defaultHeight = context.resources.getDimensionPixelSize(R.dimen.record_type_card_height)

        var width = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, defaultWidth.pxToDp())
            .dpToPx().takeUnless { it == 0 } ?: defaultWidth
        var height = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, defaultHeight.pxToDp())
            .dpToPx().takeUnless { it == 0 } ?: defaultHeight

        fun inflate(): View {
            val inflater = LayoutInflater.from(context)
            return allowVmViolations { inflater.inflate(R.layout.widget_layout, null) }
                .also { entireView = it }
        }

        // TODO this can be removed?
        val entireView: View = this.entireView ?: inflate()
        entireView.measureExactly(width = width, height = height)

        val imageView = entireView.findViewById<View>(R.id.ivWidgetBackground)
        width = imageView.measuredWidth
        height = imageView.measuredHeight
        view.measureExactly(width = width, height = height)
    }

    private fun onClick(
        context: Context?,
        intent: Intent,
    ) {
        allowDiskRead { MainScope() }.launch {
            val widgetId = intent.getIntExtra(ARGS_WIDGET_ID, 0)
            widgetTypeClickManager.onClick(
                context = context,
                recordTypeId = prefsInteractor.getWidget(widgetId),
                onWidgetUpdate = { widgetInteractor.updateSingleWidget(widgetId) },
            )
        }
    }

    private fun getPendingSelfIntent(
        context: Context,
        widgetId: Int,
    ): PendingIntent {
        val intent = Intent(context, javaClass)
        intent.action = ON_CLICK_ACTION
        intent.putExtra(ARGS_WIDGET_ID, widgetId)
        return PendingIntent.getBroadcast(context, widgetId, intent, PendingIntents.getFlags())
    }

    companion object {
        const val TYPE_IDS_EXTRA =
            "com.example.util.simpletimetracker.feature_widget.widget.typeIdsExtra"
        private const val ON_CLICK_ACTION =
            "com.example.util.simpletimetracker.feature_widget.widget.onclick"
        private const val ARGS_WIDGET_ID = "widgetId"
    }
}