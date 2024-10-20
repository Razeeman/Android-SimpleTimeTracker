package com.example.util.simpletimetracker.feature_widget.quickSettings

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.RemoteViews
import com.example.util.simpletimetracker.core.extension.allowDiskRead
import com.example.util.simpletimetracker.core.extension.allowVmViolations
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.core.utils.PendingIntents
import com.example.util.simpletimetracker.domain.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.interactor.WidgetInteractor
import com.example.util.simpletimetracker.domain.model.QuickSettingsWidgetType
import com.example.util.simpletimetracker.feature_views.extension.getBitmapFromView
import com.example.util.simpletimetracker.feature_views.extension.measureExactly
import com.example.util.simpletimetracker.feature_views.viewData.RecordTypeIcon
import com.example.util.simpletimetracker.feature_widget.R
import com.example.util.simpletimetracker.feature_widget.quickSettings.customView.WidgetQuickSettingsView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class WidgetQuickSettingsProvider : AppWidgetProvider() {

    @Inject
    lateinit var widgetInteractor: WidgetInteractor

    @Inject
    lateinit var resourceRepo: ResourceRepo

    @Inject
    lateinit var prefsInteractor: PrefsInteractor

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        if (intent?.action == ON_CLICK_ACTION) {
            onClick(intent.getIntExtra(ARGS_WIDGET_ID, 0))
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
            appWidgetIds?.forEach { prefsInteractor.removeQuickSettingsWidget(it) }
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
            val backgroundTransparency = prefsInteractor.getWidgetBackgroundTransparencyPercent()
            val name: String
            val isChecked: Boolean
            when (prefsInteractor.getQuickSettingsWidget(appWidgetId)) {
                is QuickSettingsWidgetType.AllowMultitasking -> {
                    name = resourceRepo.getString(R.string.settings_allow_multitasking)
                    isChecked = prefsInteractor.getAllowMultitasking()
                }
                is QuickSettingsWidgetType.ShowRecordTagSelection -> {
                    name = resourceRepo.getString(R.string.settings_show_record_tag_selection)
                    isChecked = prefsInteractor.getShowRecordTagSelection()
                }
            }

            val view = prepareView(
                context = context,
                name = name,
                isChecked = isChecked,
                backgroundTransparency = backgroundTransparency,
            )
            measureView(context, view)
            val bitmap = view.getBitmapFromView()

            val views = RemoteViews(context.packageName, R.layout.widget_layout)
            views.setImageViewBitmap(R.id.ivWidgetBackground, bitmap)
            views.setOnClickPendingIntent(R.id.btnWidget, getPendingSelfIntent(context, appWidgetId))

            runCatching {
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }
    }

    private fun prepareView(
        context: Context,
        name: String,
        isChecked: Boolean,
        backgroundTransparency: Long,
    ): View {
        val icon = if (isChecked) {
            RecordTypeIcon.Image(R.drawable.checkbox_checked)
        } else {
            RecordTypeIcon.Image(R.drawable.checkbox_unchecked)
        }

        val iconColor = if (isChecked) {
            resourceRepo.getThemedAttr(R.attr.colorSecondary, R.style.AppTheme)
        } else {
            resourceRepo.getColor(R.color.widget_universal_empty_color)
        }

        // TODO setting alpha on cardView doesn't work for some reason, wrap in layout before setting
        val container = allowVmViolations {
            FrameLayout(ContextThemeWrapper(context, R.style.AppTheme))
        }
        allowVmViolations {
            WidgetQuickSettingsView(ContextThemeWrapper(context, R.style.AppTheme))
        }.apply {
            cardElevation = 0f
            itemIcon = icon
            itemIconColor = iconColor
            itemName = name
            itemBackgroundAlpha = 1f - backgroundTransparency / 100f
        }.let(container::addView)

        return container
    }

    private fun measureView(context: Context, view: View) {
        var width = context.resources.getDimensionPixelSize(R.dimen.record_type_card_width)
        var height = context.resources.getDimensionPixelSize(R.dimen.record_type_card_height)
        val inflater = LayoutInflater.from(context)

        val entireView: View = allowVmViolations { inflater.inflate(R.layout.widget_layout, null) }
        entireView.measureExactly(width = width, height = height)

        val imageView = entireView.findViewById<View>(R.id.ivWidgetBackground)
        width = imageView.measuredWidth
        height = imageView.measuredHeight
        view.measureExactly(width = width, height = height)
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun onClick(
        widgetId: Int,
    ) {
        GlobalScope.launch(allowDiskRead { Dispatchers.Main }) {
            when (prefsInteractor.getQuickSettingsWidget(widgetId)) {
                is QuickSettingsWidgetType.AllowMultitasking -> {
                    val newValue = !prefsInteractor.getAllowMultitasking()
                    prefsInteractor.setAllowMultitasking(newValue)
                }
                is QuickSettingsWidgetType.ShowRecordTagSelection -> {
                    val newValue = !prefsInteractor.getShowRecordTagSelection()
                    prefsInteractor.setShowRecordTagSelection(newValue)
                }
            }
            widgetInteractor.updateQuickSettingsWidget(widgetId)
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
        private const val ON_CLICK_ACTION =
            "com.example.util.simpletimetracker.feature_widget.quickSettings.widget.onclick"
        private const val ARGS_WIDGET_ID = "widgetId"
    }
}