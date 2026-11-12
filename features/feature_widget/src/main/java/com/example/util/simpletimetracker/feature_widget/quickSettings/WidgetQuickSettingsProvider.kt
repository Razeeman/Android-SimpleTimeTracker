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
import com.example.util.simpletimetracker.core.repo.PermissionRepo
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.core.utils.PendingIntents
import com.example.util.simpletimetracker.domain.notifications.interactor.UpdateExternalViewsInteractor
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.widget.model.QuickSettingsWidgetType
import com.example.util.simpletimetracker.feature_settings.api.OnSettingChangedInteractor
import com.example.util.simpletimetracker.feature_views.extension.getBitmapFromView
import com.example.util.simpletimetracker.feature_views.extension.measureExactly
import com.example.util.simpletimetracker.feature_views.viewData.RecordTypeIcon
import com.example.util.simpletimetracker.feature_widget.R
import com.example.util.simpletimetracker.feature_widget.quickSettings.customView.WidgetQuickSettingsView
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.notification.ToastParams
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class WidgetQuickSettingsProvider : AppWidgetProvider() {

    @Inject
    lateinit var externalViewsInteractor: UpdateExternalViewsInteractor

    @Inject
    lateinit var onSettingChangedInteractor: OnSettingChangedInteractor

    @Inject
    lateinit var permissionRepo: PermissionRepo

    @Inject
    lateinit var router: Router

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
        val name: String
        val isChecked: Boolean
        val type = prefsInteractor.getQuickSettingsWidget(appWidgetId)
        when (type) {
            is QuickSettingsWidgetType.AllowMultitasking -> {
                name = resourceRepo.getString(R.string.settings_allow_multitasking)
                isChecked = prefsInteractor.getAllowMultitasking()
            }
            is QuickSettingsWidgetType.ShowRecordTagSelection -> {
                name = resourceRepo.getString(R.string.settings_show_record_tag_selection)
                isChecked = prefsInteractor.getShowRecordTagSelection()
            }
            is QuickSettingsWidgetType.ShowTimerNotifications -> {
                name = resourceRepo.getString(R.string.settings_show_notifications)
                isChecked = prefsInteractor.getShowNotifications()
            }
            is QuickSettingsWidgetType.RetroactiveTracking -> {
                name = resourceRepo.getString(R.string.settings_retroactive_tracking_mode)
                isChecked = prefsInteractor.getRetroactiveTrackingMode()
            }
            is QuickSettingsWidgetType.ShowCommentInput -> {
                name = resourceRepo.getString(R.string.settings_show_comment_input)
                isChecked = prefsInteractor.getShowCommentInput()
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
        // TODO add same settings to shortcuts, same order
        when (prefsInteractor.getQuickSettingsWidget(widgetId)) {
            is QuickSettingsWidgetType.AllowMultitasking -> {
                val newValue = !prefsInteractor.getAllowMultitasking()
                prefsInteractor.setAllowMultitasking(newValue)
                onSettingChangedInteractor.onAllowMultitaskingChange()
            }
            is QuickSettingsWidgetType.ShowRecordTagSelection -> {
                val newValue = !prefsInteractor.getShowRecordTagSelection()
                prefsInteractor.setShowRecordTagSelection(newValue)
                externalViewsInteractor.onShowRecordTagSelectionChange()
            }
            is QuickSettingsWidgetType.ShowTimerNotifications -> {
                val enable = !prefsInteractor.getShowNotifications()
                val permissionGranted = permissionRepo.areNotificationsEnabled()
                prefsInteractor.setShowNotifications(enable && permissionGranted)
                externalViewsInteractor.onShowTimerNotificationsChange()
                if (enable && !permissionGranted) {
                    val params = ToastParams(resourceRepo.getString(R.string.post_notifications))
                    router.show(params)
                }
            }
            is QuickSettingsWidgetType.RetroactiveTracking -> {
                val newValue = !prefsInteractor.getRetroactiveTrackingMode()
                prefsInteractor.setRetroactiveTrackingMode(newValue)
                onSettingChangedInteractor.onRetroactiveTrackingModeChange()
            }
            is QuickSettingsWidgetType.ShowCommentInput -> {
                val newValue = !prefsInteractor.getShowCommentInput()
                prefsInteractor.setShowCommentInput(newValue)
                externalViewsInteractor.onShowCommentInputChange()
            }
        }
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