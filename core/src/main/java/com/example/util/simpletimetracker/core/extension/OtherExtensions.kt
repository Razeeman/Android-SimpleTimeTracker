package com.example.util.simpletimetracker.core.extension

import android.content.BroadcastReceiver
import android.graphics.Rect
import android.os.StrictMode
import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.viewpager2.widget.ViewPager2
import com.example.util.simpletimetracker.core.utils.getLifecycleObserverAdapter
import com.example.util.simpletimetracker.domain.base.Coordinates
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

inline fun <T, R> T.allowDiskWrite(block: T.() -> R): R {
    val oldPolicy = StrictMode.allowThreadDiskWrites()
    try {
        return block()
    } finally {
        StrictMode.setThreadPolicy(oldPolicy)
    }
}

inline fun <T, R> T.allowDiskRead(block: T.() -> R): R {
    val oldPolicy = StrictMode.allowThreadDiskReads()
    try {
        return block()
    } finally {
        StrictMode.setThreadPolicy(oldPolicy)
    }
}

// Used to block StrictMode assertConfigurationContext log error.
inline fun <T, R> T.allowVmViolations(block: T.() -> R): R {
    val oldPolicy = StrictMode.getVmPolicy()
    try {
        StrictMode.setVmPolicy(StrictMode.VmPolicy.LAX)
        return block()
    } finally {
        StrictMode.setVmPolicy(oldPolicy)
    }
}

fun BroadcastReceiver.goAsync(
    block: suspend () -> Unit,
) {
    val result = goAsync()
    allowDiskRead { MainScope() }.launch {
        try {
            block()
        } finally {
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
