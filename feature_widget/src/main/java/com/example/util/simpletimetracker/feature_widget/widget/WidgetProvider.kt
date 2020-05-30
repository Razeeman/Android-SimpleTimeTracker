package com.example.util.simpletimetracker.feature_widget.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.widget.RemoteViews
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.IconMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.interactor.*
import com.example.util.simpletimetracker.feature_widget.R
import com.example.util.simpletimetracker.feature_widget.di.WidgetComponentProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class WidgetProvider : AppWidgetProvider() {

    @Inject
    lateinit var runningRecordInteractor: RunningRecordInteractor
    @Inject
    lateinit var recordTypeInteractor: RecordTypeInteractor
    @Inject
    lateinit var recordInteractor: RecordInteractor
    @Inject
    lateinit var widgetInteractor: WidgetInteractor
    @Inject
    lateinit var colorMapper: ColorMapper
    @Inject
    lateinit var iconMapper: IconMapper
    @Inject
    lateinit var resourceRepo: ResourceRepo
    @Inject
    lateinit var prefsInteractor: PrefsInteractor

    override fun onReceive(context: Context?, intent: Intent?) {
        (context?.applicationContext as? WidgetComponentProvider)
            ?.widgetComponent
            ?.inject(this)

        super.onReceive(context, intent)
        if (intent?.action == ON_CLICK_ACTION) {
            onClick(intent.getIntExtra(ARGS_WIDGET_ID, 0))
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

        CoroutineScope(Dispatchers.Main).launch {
            val views = RemoteViews(
                context.packageName,
                R.layout.widget_layout
            )

            val recordTypeId = prefsInteractor.getWidget(appWidgetId)
            val recordType = recordTypeInteractor.get(recordTypeId)
                ?.takeUnless { it.hidden }
            val runningRecord = runningRecordInteractor.get(recordTypeId)

            val icon = recordType?.icon
                ?.let(iconMapper::mapToDrawableResId)
                ?: R.drawable.unknown

            val name = recordType?.name
                ?: R.string.widget_load_error.let(resourceRepo::getString)

            val color = if (runningRecord != null && recordType != null) {
                recordType.color
                    .let(colorMapper::mapToColorResId)
                    .let(resourceRepo::getColor)
            } else {
                Color.BLACK
            }

            val alpha = if (runningRecord != null && recordType != null) {
                ENABLED_ALPHA
            } else {
                DISABLED_ALPHA
            }

            views.setTextViewText(R.id.widgetText, name)
            views.setImageViewResource(R.id.ivWidgetIcon, icon)
            views.setInt(R.id.ivWidgetBackground, "setColorFilter", color)
            views.setInt(R.id.ivWidgetBackground, "setImageAlpha", alpha)
            views.setOnClickPendingIntent(
                R.id.ivWidgetBackground,
                getPendingSelfIntent(context, appWidgetId)
            )

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    private fun onClick(widgetId: Int) {
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
                recordInteractor.add(
                    typeId = recordTypeId,
                    timeStarted = runningRecord.timeStarted
                )
                runningRecordInteractor.remove(runningRecord.id)
            } else {
                // Add new running record
                runningRecordInteractor.add(recordTypeId)
            }

            widgetInteractor.updateWidget(widgetId)
        }
    }

    private fun getPendingSelfIntent(
        context: Context,
        widgetId: Int
    ): PendingIntent {
        val intent = Intent(context, javaClass)
        intent.action = ON_CLICK_ACTION
        intent.putExtra(ARGS_WIDGET_ID, widgetId)
        return PendingIntent.getBroadcast(context, widgetId, intent, 0)
    }

    companion object {
        private const val ON_CLICK_ACTION =
            "com.example.util.simpletimetracker.feature_widget.widget.onclick"
        private const val ARGS_WIDGET_ID = "widgetId"

        private const val ENABLED_ALPHA = 255
        private const val DISABLED_ALPHA = 100
    }
}