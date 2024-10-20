package com.example.util.simpletimetracker.feature_widget.single

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RemoteViews
import com.example.util.simpletimetracker.core.extension.allowDiskRead
import com.example.util.simpletimetracker.core.extension.allowVmViolations
import com.example.util.simpletimetracker.core.interactor.CompleteTypesStateInteractor
import com.example.util.simpletimetracker.core.interactor.FilterGoalsByDayOfWeekInteractor
import com.example.util.simpletimetracker.core.interactor.GetCurrentRecordsDurationInteractor
import com.example.util.simpletimetracker.core.interactor.RecordRepeatInteractor
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.IconMapper
import com.example.util.simpletimetracker.core.mapper.RecordTypeViewDataMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.core.utils.PendingIntents
import com.example.util.simpletimetracker.domain.REPEAT_BUTTON_ITEM_ID
import com.example.util.simpletimetracker.domain.extension.getDaily
import com.example.util.simpletimetracker.domain.extension.orFalse
import com.example.util.simpletimetracker.domain.interactor.AddRunningRecordMediator
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeGoalInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.interactor.RemoveRunningRecordMediator
import com.example.util.simpletimetracker.domain.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.domain.interactor.WidgetInteractor
import com.example.util.simpletimetracker.feature_views.ColorUtils
import com.example.util.simpletimetracker.feature_views.RecordTypeView
import com.example.util.simpletimetracker.feature_views.extension.getBitmapFromView
import com.example.util.simpletimetracker.feature_views.extension.measureExactly
import com.example.util.simpletimetracker.feature_views.extension.setAllMargins
import com.example.util.simpletimetracker.feature_views.viewData.RecordTypeIcon
import com.example.util.simpletimetracker.feature_widget.R
import com.example.util.simpletimetracker.feature_widget.interactor.WidgetViewsHolder
import com.example.util.simpletimetracker.navigation.params.screen.RecordTagSelectionParams
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class WidgetSingleProvider : AppWidgetProvider() {

    @Inject
    lateinit var addRunningRecordMediator: AddRunningRecordMediator

    @Inject
    lateinit var removeRunningRecordMediator: RemoveRunningRecordMediator

    @Inject
    lateinit var runningRecordInteractor: RunningRecordInteractor

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
    lateinit var recordRepeatInteractor: RecordRepeatInteractor

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
        if (intent?.action == ON_CLICK_ACTION) {
            onClick(context, intent.getIntExtra(ARGS_WIDGET_ID, 0))
        }
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

    @OptIn(DelicateCoroutinesApi::class)
    override fun onDeleted(context: Context?, appWidgetIds: IntArray?) {
        GlobalScope.launch(allowDiskRead { Dispatchers.Main }) {
            appWidgetIds?.forEach { prefsInteractor.removeWidget(it) }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun updateAppWidget(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetId: Int,
    ) {
        if (context == null || appWidgetManager == null) return

        GlobalScope.launch(allowDiskRead { Dispatchers.Main }) {
            val view: View
            val recordTypeId = prefsInteractor.getWidget(appWidgetId)
            val backgroundTransparency = prefsInteractor.getWidgetBackgroundTransparencyPercent()
            val typeIds = typeIdsToUpdate
            if (typeIds.isNotEmpty() && recordTypeId !in typeIds) return@launch
            val runningRecord = if (runningRecordInteractor.has(recordTypeId)) {
                runningRecordInteractor.get(recordTypeId)
            } else {
                null
            }
            val isDarkTheme: Boolean = prefsInteractor.getDarkMode()

            if (recordTypeId == REPEAT_BUTTON_ITEM_ID) {
                val viewData = recordTypeViewDataMapper.mapToRepeatItem(
                    numberOfCards = 0,
                    isDarkTheme = isDarkTheme,
                )
                view = prepareView(
                    context = context,
                    recordTypeIcon = viewData.iconId,
                    recordTypeName = viewData.name,
                    recordTypeColor = viewData.color,
                    isRunning = false,
                    isChecked = null,
                    isComplete = false,
                    backgroundTransparency = backgroundTransparency,
                )
            } else {
                val recordType = recordTypeInteractor.get(recordTypeId)
                val goal = filterGoalsByDayOfWeekInteractor
                    .execute(recordTypeGoalInteractor.getByType(recordTypeId))
                    .getDaily()
                val dailyCurrent = if (goal != null) {
                    getCurrentRecordsDurationInteractor.getDailyCurrent(
                        typeId = recordTypeId,
                        runningRecord = runningRecord,
                    )
                } else {
                    null
                }
                val isChecked = if (recordType != null) {
                    recordTypeViewDataMapper.mapGoalCheckmark(
                        goal = goal,
                        dailyCurrent = dailyCurrent,
                    )
                } else {
                    null
                }
                view = prepareView(
                    context = context,
                    recordTypeIcon = recordType?.icon
                        ?.let(iconMapper::mapIcon),
                    recordTypeName = recordType?.name,
                    recordTypeColor = recordType?.color
                        ?.let { colorMapper.mapToColorInt(it, isDarkTheme) },
                    isRunning = runningRecord != null && recordType != null,
                    isChecked = isChecked,
                    isComplete = recordTypeId in completeTypesStateInteractor.widgetTypeIds,
                    backgroundTransparency = backgroundTransparency,
                )
            }

            measureView(context, view)
            val bitmap = view.getBitmapFromView()

            val views = RemoteViews(context.packageName, R.layout.widget_layout)
            if (runningRecord != null) {
                val timeStarted = runningRecord.timeStarted
                val base = SystemClock.elapsedRealtime() - (System.currentTimeMillis() - timeStarted)
                views.setChronometer(R.id.timerWidget, base, null, true)
                views.setViewVisibility(R.id.timerWidget, View.VISIBLE)
            } else {
                views.setViewVisibility(R.id.timerWidget, View.GONE)
            }
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
        isRunning: Boolean,
        isChecked: Boolean?,
        isComplete: Boolean,
        backgroundTransparency: Long,
    ): View {
        val icon = recordTypeIcon
            ?: RecordTypeIcon.Image(R.drawable.unknown)

        val name = recordTypeName
            ?: R.string.widget_load_error.let(resourceRepo::getString)

        val textColor = if (isRunning) {
            resourceRepo.getColor(R.color.colorIcon)
        } else {
            resourceRepo.getColor(R.color.widget_universal_empty_color)
        }

        val color = if (isRunning && recordTypeColor != null) {
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
            itemWithCheck = isChecked != null
            itemIsChecked = isChecked.orFalse()
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

    private fun measureView(context: Context, view: View) {
        var width = context.resources.getDimensionPixelSize(R.dimen.record_type_card_width)
        var height = context.resources.getDimensionPixelSize(R.dimen.record_type_card_height)

        fun inflate(): View {
            val inflater = LayoutInflater.from(context)
            return allowVmViolations { inflater.inflate(R.layout.widget_layout, null) }
                .also { entireView = it }
        }

        val entireView: View = this.entireView ?: inflate()
        entireView.measureExactly(width = width, height = height)

        val imageView = entireView.findViewById<View>(R.id.ivWidgetBackground)
        width = imageView.measuredWidth
        height = imageView.measuredHeight
        view.measureExactly(width = width, height = height)
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun onClick(
        context: Context?,
        widgetId: Int,
    ) {
        GlobalScope.launch(allowDiskRead { Dispatchers.Main }) {
            val recordTypeId = prefsInteractor.getWidget(widgetId)

            if (recordTypeId == REPEAT_BUTTON_ITEM_ID) {
                recordRepeatInteractor.repeatExternal()
                return@launch
            }

            val type = recordTypeInteractor.get(recordTypeId)

            // If recordType removed - update widget and exit
            if (type == null) {
                widgetInteractor.updateSingleWidget(widgetId)
                return@launch
            }

            if (type.defaultDuration > 0) {
                completeTypesStateInteractor.widgetTypeIds += recordTypeId
                widgetInteractor.updateSingleWidget(widgetId)
                delay(1000)
                completeTypesStateInteractor.widgetTypeIds -= recordTypeId
                widgetInteractor.updateSingleWidget(widgetId)
            }

            val runningRecord = runningRecordInteractor.get(recordTypeId)
            if (runningRecord != null) {
                // Stop running record, add new record
                removeRunningRecordMediator.removeWithRecordAdd(runningRecord)
            } else {
                // Start running record
                addRunningRecordMediator.tryStartTimer(
                    typeId = recordTypeId,
                    onNeedToShowTagSelection = { showTagSelection(context, recordTypeId) },
                )
            }
        }
    }

    private fun showTagSelection(context: Context?, typeId: Long) {
        context ?: return

        WidgetSingleTagSelectionActivity.getStartIntent(
            context = context,
            data = RecordTagSelectionParams(typeId),
        ).let(context::startActivity)
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