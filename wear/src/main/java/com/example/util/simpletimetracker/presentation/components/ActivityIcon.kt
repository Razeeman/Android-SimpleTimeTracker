package com.example.util.simpletimetracker.presentation.components

import android.util.TypedValue
import android.view.Gravity
import androidx.appcompat.widget.AppCompatTextView
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.widget.TextViewCompat
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.LocalContentColor
import com.example.util.simpletimetracker.R

@Composable
fun ActivityIcon(
    activityIcon: String,
) {
    val context = LocalContext.current
    val iconIsImage = activityIcon.startsWith("ic_")

    if (iconIsImage) {
        val iconDrawableRes = context.resources
            .getIdentifier(activityIcon, "drawable", context.packageName)
            .takeIf { it != 0 }
            ?: R.drawable.unknown
        Icon(
            painter = painterResource(iconDrawableRes),
            contentDescription = null,
            modifier = Modifier
                .fillMaxHeight()
                .height(1.dp),
        )
    } else {
        val textColor = LocalContentColor.current.toArgb()
        AndroidView(
            factory = { context ->
                val view = AppCompatTextView(context)
                view.gravity = Gravity.CENTER
                view.setTextColor(textColor)
                TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
                    view,
                    1,
                    100,
                    1,
                    TypedValue.COMPLEX_UNIT_SP,
                )
                view
            },
            modifier = Modifier
                .aspectRatio(1f)
                .width(0.dp),
            update = {
                it.text = activityIcon
            },
        )
    }
}