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
import com.example.util.simpletimetracker.core.mapper.QuickSettingViewDataMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.core.utils.PendingIntents
import com.example.util.simpletimetracker.domain.prefs.interactor.IsQuickSettingEnabledInteractor
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.feature_settings.api.OnQuickSettingClickInteractor
import com.example.util.simpletimetracker.feature_views.extension.getBitmapFromView
import com.example.util.simpletimetracker.feature_views.extension.measureExactly
import com.example.util.simpletimetracker.feature_views.viewData.RecordTypeIcon
import com.example.util.simpletimetracker.feature_widget.R
import com.example.util.simpletimetracker.feature_widget.quickSettings.customView.WidgetQuickSettingsView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class WidgetQuickSettingsProvider : AppWidgetProvider() {

    @Inject
    lateinit var onQuickSettingClickInteractor: OnQuickSettingClickInteractor

    @Inject
    lateinit var isQuickSettingEnabledInteractor: IsQuickSettingEnabledInteractor

    @Inject
    lateinit var quickSettingViewDataMapper: QuickSettingViewDataMapper

    @Inject
    lateinit var resourceRepo: ResourceRepo

    @Inject
    lateinit var prefsInteractor: PrefsInteractor

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        if (intent?.action == ON_CLICK_ACTION) {
            launchAsync {
                onClick(intent.getIntExtra(ARGS_WIDGET_ID, 0))
            }
        }
    }

    override fun onUpdate(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetIds: IntArray?,
    ) {
        launchAsync {
            appWidgetIds?.forEach { widgetId ->
                updateAppWidget(context, appWidgetManager, widgetId)
            }
        }
    }

    override fun onDeleted(context: Context?, appWidgetIds: IntArray?) {
        launchAsync {
            appWidgetIds?.forEach { prefsInteractor.removeQuickSettingsWidget(it) }
        }
    }

    private suspend fun updateAppWidget(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetId: Int,
    ) {
        if (context == null || appWidgetManager == null) return

        val backgroundTransparency = prefsInteractor.getWidgetBackgroundTransparencyPercent()
        val type = prefsInteractor.getQuickSettingsWidget(appWidgetId)
        val name = quickSettingViewDataMapper.mapTitle(type)
        val isChecked = isQuickSettingEnabledInteractor.execute(type)

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
            resourceRepo.getThemedAttr(R.attr.colorSecondary, false)
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

    private suspend fun onClick(widgetId: Int) {
        val type = prefsInteractor.getQuickSettingsWidget(widgetId)
        onQuickSettingClickInteractor.execute(type)
    }

    private fun launchAsync(block: suspend () -> Unit) {
        val pendingResult = goAsync()
        allowDiskRead { MainScope() }.launch {
            try {
                block()
            } catch (e: Exception) {
                Timber.e(e)
            } finally {
                pendingResult.finish()
            }
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