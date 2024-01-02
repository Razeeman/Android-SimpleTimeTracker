package com.example.util.simpletimetracker.feature_widget.single

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.RemoteViews
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
import com.example.util.simpletimetracker.domain.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTagInteractor
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
import com.example.util.simpletimetracker.navigation.params.screen.RecordTagSelectionParams
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
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
    lateinit var recordInteractor: RecordInteractor

    @Inject
    lateinit var recordTagInteractor: RecordTagInteractor

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

    private var typeIdsToUpdate: List<Long> = emptyList()

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

    override fun onDeleted(context: Context?, appWidgetIds: IntArray?) {
        GlobalScope.launch(Dispatchers.Main) {
            appWidgetIds?.forEach { prefsInteractor.removeWidget(it) }
        }
    }

    private fun updateAppWidget(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetId: Int,
    ) {
        if (context == null || appWidgetManager == null) return

        GlobalScope.launch(Dispatchers.Main) {
            val view: View
            val recordTypeId = prefsInteractor.getWidget(appWidgetId)
            val backgroundTransparency = prefsInteractor.getWidgetBackgroundTransparencyPercent()
            val typeIds = typeIdsToUpdate
            if (typeIds.isNotEmpty() && recordTypeId !in typeIds) return@launch
            val runningRecord = runningRecordInteractor.get(recordTypeId)
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
                    isChecked = false,
                    backgroundTransparency = backgroundTransparency,
                )
            } else {
                val recordType = recordTypeInteractor.get(recordTypeId)
                    ?.takeUnless { it.hidden }
                val goal = recordTypeGoalInteractor.getByType(recordTypeId).getDaily()
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

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    private fun prepareView(
        context: Context,
        recordTypeIcon: RecordTypeIcon?,
        recordTypeName: String?,
        recordTypeColor: Int?,
        isRunning: Boolean,
        isChecked: Boolean?,
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

        // TODO setting alpha on cardView doesn't work for some reason, wrap in layout before setting
        val container = FrameLayout(ContextThemeWrapper(context, R.style.AppTheme))
        RecordTypeView(ContextThemeWrapper(context, R.style.AppTheme)).apply {
            getContainer().radius = resources.getDimensionPixelOffset(R.dimen.widget_universal_corner_radius).toFloat()
            getContainer().cardElevation = 0f
            getContainer().useCompatPadding = false
            getCheckmarkOutline().setAllMargins(4)
            itemIcon = icon
            itemName = name
            itemIconColor = textColor
            itemColor = color
            itemWithCheck = isChecked != null
            itemIsChecked = isChecked.orFalse()
        }.let(container::addView)

        return container
    }

    private fun measureView(context: Context, view: View) {
        var width = context.resources.getDimensionPixelSize(R.dimen.record_type_card_width)
        var height = context.resources.getDimensionPixelSize(R.dimen.record_type_card_height)
        val inflater = LayoutInflater.from(context)

        val entireView: View = inflater.inflate(R.layout.widget_layout, null)
        entireView.measureExactly(width = width, height = height)

        val imageView = entireView.findViewById<View>(R.id.ivWidgetBackground)
        width = imageView.measuredWidth
        height = imageView.measuredHeight
        view.measureExactly(width = width, height = height)
    }

    private fun onClick(
        context: Context?,
        widgetId: Int,
    ) {
        GlobalScope.launch(Dispatchers.Main) {
            val recordTypeId = prefsInteractor.getWidget(widgetId)

            if (recordTypeId == REPEAT_BUTTON_ITEM_ID) {
                recordRepeatInteractor.repeatExternal()
                return@launch
            }

            // If recordType removed - update widget and exit
            recordTypeInteractor.get(recordTypeId)
                ?.takeUnless { it.hidden }
                ?: run {
                    widgetInteractor.updateSingleWidget(widgetId)
                    return@launch
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