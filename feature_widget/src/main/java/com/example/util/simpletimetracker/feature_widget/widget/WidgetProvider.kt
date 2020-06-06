package com.example.util.simpletimetracker.feature_widget.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.View.MeasureSpec
import android.widget.RemoteViews
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.IconMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.core.view.RecordTypeView
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.domain.interactor.WidgetInteractor
import com.example.util.simpletimetracker.feature_widget.R
import com.example.util.simpletimetracker.feature_widget.di.WidgetComponentProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
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

        val view = prepareView(context, appWidgetId)
        measureView(context, view)
        val bitmap = getBitmapFromView(view)

        val views = RemoteViews(context.packageName, R.layout.widget_layout)
        views.setImageViewBitmap(R.id.ivWidgetBackground, bitmap)
        views.setOnClickPendingIntent(R.id.btnWidget, getPendingSelfIntent(context, appWidgetId))

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun prepareView(
        context: Context,
        appWidgetId: Int
    ): View = runBlocking {
        val recordTypeId = prefsInteractor.getWidget(appWidgetId)
        val recordType = recordTypeInteractor.get(recordTypeId)?.takeUnless { it.hidden }
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

        RecordTypeView(ContextThemeWrapper(context, R.style.AppTheme)).apply {
            itemUseCompatPadding = false
            itemIcon = icon
            itemName = name
            itemColor = color
            itemAlpha = alpha
        }
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

    private fun getBitmapFromView(view: View): Bitmap {
        return Bitmap.createBitmap(
            view.measuredWidth,
            view.measuredHeight,
            Bitmap.Config.ARGB_8888
        ).also {
            view.draw(Canvas(it))
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

        private const val ENABLED_ALPHA = 1f
        private const val DISABLED_ALPHA = 0.5f
    }
}