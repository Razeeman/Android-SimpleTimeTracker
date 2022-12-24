package com.example.util.simpletimetracker.feature_widget.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.SystemClock
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.View.MeasureSpec
import android.widget.FrameLayout
import android.widget.RemoteViews
import com.example.util.simpletimetracker.feature_views.extension.getBitmapFromView
import com.example.util.simpletimetracker.domain.interactor.AddRunningRecordMediator
import com.example.util.simpletimetracker.domain.interactor.RemoveRunningRecordMediator
import com.example.util.simpletimetracker.domain.interactor.WidgetInteractor
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.IconMapper
import com.example.util.simpletimetracker.core.mapper.RecordTypeViewDataMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.core.utils.PendingIntents
import com.example.util.simpletimetracker.feature_views.RecordTypeView
import com.example.util.simpletimetracker.feature_views.viewData.RecordTypeIcon
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTagInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.domain.model.RecordType
import com.example.util.simpletimetracker.domain.model.RunningRecord
import com.example.util.simpletimetracker.feature_widget.R
import com.example.util.simpletimetracker.navigation.params.screen.RecordTagSelectionParams
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class WidgetProvider : AppWidgetProvider() {

    @Inject
    lateinit var addRunningRecordMediator: AddRunningRecordMediator

    @Inject
    lateinit var removeRunningRecordMediator: RemoveRunningRecordMediator

    @Inject
    lateinit var runningRecordInteractor: RunningRecordInteractor

    @Inject
    lateinit var recordTypeInteractor: RecordTypeInteractor

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

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        if (intent?.action == ON_CLICK_ACTION) {
            onClick(context, intent.getIntExtra(ARGS_WIDGET_ID, 0))
        }
    }

    override fun onUpdate(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetIds: IntArray?
    ) {
        appWidgetIds?.forEach { widgetId ->
            updateAppWidget(context, appWidgetManager, widgetId)
        }
    }

    override fun onDeleted(context: Context?, appWidgetIds: IntArray?) {
        CoroutineScope(Dispatchers.Main).launch {
            appWidgetIds?.forEach { prefsInteractor.removeWidget(it) }
        }
    }

    private fun updateAppWidget(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetId: Int
    ) {
        if (context == null || appWidgetManager == null) return

        var recordType: RecordType?
        var runningRecord: RunningRecord?
        var isDarkTheme: Boolean
        runBlocking {
            val recordTypeId = prefsInteractor.getWidget(appWidgetId)
            recordType = recordTypeInteractor.get(recordTypeId)?.takeUnless { it.hidden }
            runningRecord = runningRecordInteractor.get(recordTypeId)
            isDarkTheme = prefsInteractor.getDarkMode()
        }

        val view = prepareView(context, recordType, runningRecord, isDarkTheme)
        measureView(context, view)
        val bitmap = view.getBitmapFromView()

        val views = RemoteViews(context.packageName, R.layout.widget_layout)
        if (runningRecord != null) {
            val timeStarted = runningRecord?.timeStarted.orZero()
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

    private fun prepareView(
        context: Context,
        recordType: RecordType?,
        runningRecord: RunningRecord?,
        isDarkTheme: Boolean,
    ): View {
        val icon = recordType?.icon
            ?.let(iconMapper::mapIcon)
            ?: RecordTypeIcon.Image(R.drawable.unknown)

        val name = recordType?.name
            ?: R.string.widget_load_error.let(resourceRepo::getString)

        val color = if (runningRecord != null && recordType != null) {
            colorMapper.mapToColorInt(recordType.color, isDarkTheme)
        } else {
            Color.BLACK
        }

        val viewAlpha = if (runningRecord != null && recordType != null) {
            ENABLED_ALPHA
        } else {
            DISABLED_ALPHA
        }

        // TODO setting alpha on cardView doesn't work for some reason, wrap in layout before setting
        val container = FrameLayout(ContextThemeWrapper(context, R.style.AppTheme))
        RecordTypeView(ContextThemeWrapper(context, R.style.AppTheme)).apply {
            cardElevation = 0f
            useCompatPadding = false
            itemIcon = icon
            itemName = name
            itemColor = color
            alpha = viewAlpha
        }.let(container::addView)

        return container
    }

    private fun measureView(context: Context, view: View) {
        var width = context.resources.getDimensionPixelSize(R.dimen.record_type_card_width)
        var height = context.resources.getDimensionPixelSize(R.dimen.record_type_card_height)
        val inflater = LayoutInflater.from(context)

        val entireView: View = inflater.inflate(R.layout.widget_layout, null)
        var specWidth = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY)
        var specHeight = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
        entireView.measure(specWidth, specHeight)
        entireView.layout(0, 0, entireView.measuredWidth, entireView.measuredHeight)

        val imageView = entireView.findViewById<View>(R.id.ivWidgetBackground)
        width = imageView.measuredWidth
        height = imageView.measuredHeight
        specWidth = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY)
        specHeight = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
        view.measure(specWidth, specHeight)
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)
    }

    private fun onClick(
        context: Context?,
        widgetId: Int
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            val recordTypeId = prefsInteractor.getWidget(widgetId)

            // If recordType removed - update widget and exit
            recordTypeInteractor.get(recordTypeId)
                ?.takeUnless { it.hidden }
                ?: run {
                    widgetInteractor.updateWidget(widgetId)
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
                    onNeedToShowTagSelection = { showTagSelection(context, recordTypeId) }
                )
            }
        }
    }

    private fun showTagSelection(context: Context?, typeId: Long) {
        context ?: return

        WidgetTagSelectionActivity.getStartIntent(
            context = context,
            data = RecordTagSelectionParams(typeId)
        ).let(context::startActivity)
    }

    private fun getPendingSelfIntent(
        context: Context,
        widgetId: Int
    ): PendingIntent {
        val intent = Intent(context, javaClass)
        intent.action = ON_CLICK_ACTION
        intent.putExtra(ARGS_WIDGET_ID, widgetId)
        return PendingIntent.getBroadcast(context, widgetId, intent, PendingIntents.getFlags())
    }

    companion object {
        private const val ON_CLICK_ACTION =
            "com.example.util.simpletimetracker.feature_widget.widget.onclick"
        private const val ARGS_WIDGET_ID = "widgetId"

        private const val ENABLED_ALPHA = 1f
        private const val DISABLED_ALPHA = 0.5f
    }
}