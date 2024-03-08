/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.widget.TextViewCompat
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.LocalContentColor
import com.example.util.simpletimetracker.domain.WearActivityIcon

@Composable
fun ActivityIcon(
    activityIcon: WearActivityIcon,
) {
    when (activityIcon) {
        is WearActivityIcon.Image -> {
            Icon(
                painter = painterResource(activityIcon.iconId),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxHeight()
                    .height(1.dp),
            )
        }
        is WearActivityIcon.Text -> {
            val textColor = LocalContentColor.current.toArgb()
            AndroidView(
                factory = { ctx ->
                    val view = AppCompatTextView(ctx)
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
                    it.text = activityIcon.text
                },
            )
        }
    }
}