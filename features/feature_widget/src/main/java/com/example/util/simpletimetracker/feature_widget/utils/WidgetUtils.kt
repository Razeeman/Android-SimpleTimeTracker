package com.example.util.simpletimetracker.feature_widget.utils

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.SystemClock
import android.view.View
import android.widget.RemoteViews
import com.example.util.simpletimetracker.domain.record.model.Record
import com.example.util.simpletimetracker.domain.record.model.RunningRecord
import com.example.util.simpletimetracker.feature_widget.R

fun Intent?.getAppWidgetIdOrInvalid(): Int {
    return this?.getAppWidgetIdOrNull() ?: AppWidgetManager.INVALID_APPWIDGET_ID
}

fun Intent?.getAppWidgetIdOrNull(): Int? {
    return this?.getIntExtra(
        AppWidgetManager.EXTRA_APPWIDGET_ID,
        AppWidgetManager.INVALID_APPWIDGET_ID,
    )
}

fun setChronometer(
    timestamp: Long?,
    chronometerId: Int,
    views: RemoteViews,
    started: Boolean,
) {
    if (timestamp != null) {
        val base = SystemClock.elapsedRealtime() - timestamp
        views.setChronometer(chronometerId, base, null, started)
        views.setViewVisibility(chronometerId, View.VISIBLE)
    } else {
        views.setViewVisibility(chronometerId, View.GONE)
    }
}

fun setRecordTypeTimers(
    runningRecord: RunningRecord?,
    prevRecord: Record?,
    views: RemoteViews,
) {
    when {
        runningRecord != null -> {
            val base = System.currentTimeMillis() - runningRecord.timeStarted
            setChronometer(base, R.id.timerWidget, views, true)
            views.setViewVisibility(R.id.timerWidget2, View.GONE)
        }
        prevRecord != null -> {
            val base1 = System.currentTimeMillis() - prevRecord.timeEnded
            val base2 = prevRecord.timeEnded - prevRecord.timeStarted
            setChronometer(base1, R.id.timerWidget, views, true)
            setChronometer(base2, R.id.timerWidget2, views, false)
        }
        else -> {
            views.setViewVisibility(R.id.timerWidget, View.GONE)
            views.setViewVisibility(R.id.timerWidget2, View.GONE)
        }
    }
}