/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.example.util.simpletimetracker.complication

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.view.isVisible
import com.example.util.simpletimetracker.databinding.WearIconViewLayoutBinding
import com.example.util.simpletimetracker.domain.WearActivityIcon

class WearIconView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(
    context,
    attrs,
    defStyleAttr,
) {

    private val binding: WearIconViewLayoutBinding = WearIconViewLayoutBinding
        .inflate(LayoutInflater.from(context), this)

    var itemIcon: WearActivityIcon = WearActivityIcon.Image(0)
        set(value) {
            when (value) {
                is WearActivityIcon.Image -> setImageIcon(value.iconId)
                is WearActivityIcon.Text -> setTextIcon(value.text)
            }
            field = value
        }

    private fun setImageIcon(value: Int) = with(binding) {
        ivIconViewImage.setBackgroundResource(value)
        ivIconViewImage.tag = value

        ivIconViewImage.isVisible = true
        tvIconViewEmoji.isVisible = false
    }

    private fun setTextIcon(value: String) = with(binding) {
        tvIconViewEmoji.text = value

        tvIconViewEmoji.isVisible = true
        ivIconViewImage.isVisible = false
    }
}