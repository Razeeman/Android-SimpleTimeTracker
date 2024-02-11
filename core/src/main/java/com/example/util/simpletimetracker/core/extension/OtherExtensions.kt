package com.example.util.simpletimetracker.core.extension

import android.content.BroadcastReceiver
import android.graphics.Rect
import android.os.StrictMode
import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.viewpager2.widget.ViewPager2
import com.example.util.simpletimetracker.core.utils.getLifecycleObserverAdapter
import com.example.util.simpletimetracker.domain.model.Coordinates
import java.util.Calendar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

inline fun <T, R> T.allowDiskWrite(block: T.() -> R): R {
    val oldPolicy = StrictMode.allowThreadDiskWrites()
    try {
        return block()
    } finally {
        StrictMode.setThreadPolicy(oldPolicy)
    }
}

fun Calendar.setWeekToFirstDay() {
    val another = Calendar.getInstance()
    another.timeInMillis = timeInMillis

    val currentTime = another.timeInMillis
    // Setting DAY_OF_WEEK have a weird behaviour so as if after that another field is set -
    // it would reset to current day. Use another calendar to manipulate day of week and get its time.
    another.set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
    // If went to future - go back a week
    if (another.timeInMillis > currentTime) {
        another.add(Calendar.DATE, -7)
    }

    timeInMillis = another.timeInMillis
}

fun Calendar.setToStartOfDay() {
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
}

fun Calendar.shift(shift: Long): Calendar {
    // Example
    // shift 6h
    // before 2023-03-26T00:00+01:00[Europe/Amsterdam] DST_OFFSET = 0
    // after 2023-03-26T07:00+02:00[Europe/Amsterdam] DST_OFFSET = 3600000
    // need to compensate one hour.
    val dstOffsetBefore = get(Calendar.DST_OFFSET)
    timeInMillis += shift
    val dstOffsetAfter = get(Calendar.DST_OFFSET)
    timeInMillis += (dstOffsetBefore - dstOffsetAfter)
    return this
}

fun Calendar.shiftTimeStamp(timestamp: Long, shift: Long): Long {
    timeInMillis = timestamp
    shift(shift)
    return timeInMillis
}

@OptIn(DelicateCoroutinesApi::class)
fun BroadcastReceiver.goAsync(
    coroutineScope: CoroutineScope = GlobalScope,
    finally: () -> Unit,
    block: suspend () -> Unit,
) {
    val result = goAsync()
    coroutineScope.launch {
        try {
            block()
        } finally {
            finally()
            // Always call finish(), even if the coroutineScope was cancelled
            result.finish()
        }
    }
}

fun ViewPager2.addOnPageChangeCallback(
    lifecycleOwner: LifecycleOwner,
    onPageScrolled: (Int, Float, Int) -> Unit = { _, _, _ -> },
    onPageSelected: (Int) -> Unit = {},
    onPageScrollStateChanged: (Int) -> Unit = {},
) {
    val callback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int,
        ) = onPageScrolled(position, positionOffset, positionOffsetPixels)

        override fun onPageSelected(position: Int) = onPageSelected(position)
        override fun onPageScrollStateChanged(state: Int) = onPageScrollStateChanged(state)
    }

    registerOnPageChangeCallback(callback)
    getLifecycleObserverAdapter(
        onDestroy = { unregisterOnPageChangeCallback(callback) },
    ).let(lifecycleOwner.lifecycle::addObserver)
}

fun View.getCoordinates(): Coordinates {
    val rect = Rect()
    getGlobalVisibleRect(rect)
    return Coordinates(
        left = rect.left,
        top = rect.top,
        right = rect.right,
        bottom = rect.bottom,
    )
}