package com.example.util.simpletimetracker.feature_statistics_detail.adapter

import android.content.res.ColorStateList
import android.graphics.Typeface
import android.util.TypedValue
import androidx.core.view.ViewCompat
import com.example.util.simpletimetracker.core.extension.getCoordinates
import com.example.util.simpletimetracker.domain.model.Coordinates
import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_views.extension.getThemedAttr
import com.example.util.simpletimetracker.feature_statistics_detail.R
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.feature_views.extension.visible
import com.example.util.simpletimetracker.feature_statistics_detail.databinding.StatisticsDetailCardInternalItemBinding as Binding
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailCardInternalViewData as ViewData

fun createStatisticsDetailCardInternalAdapterDelegate(
    onItemClick: (ViewData.ClickableType, Coordinates) -> Unit,
) = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate,
) { binding, item, _ ->

    with(binding) {
        item as ViewData

        tvStatisticsDetailCardValue.text = item.value
        tvStatisticsDetailCardValue.setTextSize(TypedValue.COMPLEX_UNIT_SP, item.titleTextSizeSp.toFloat())
        if (item.accented) {
            tvStatisticsDetailCardValue.typeface =
                Typeface.create("sans-serif-condensed", Typeface.BOLD)
            tvStatisticsDetailCardValue
                .setTextColor(root.context.getThemedAttr(R.attr.colorSecondary))
            tvStatisticsDetailCardValue.letterSpacing = -0.05f
        }

        when (item.valueChange) {
            is ViewData.ValueChange.None -> {
                tvStatisticsDetailCardValueChange.visible = false
            }
            is ViewData.ValueChange.Present -> {
                tvStatisticsDetailCardValueChange.visible = true
                tvStatisticsDetailCardValueChange.text = item.valueChange.text
                tvStatisticsDetailCardValueChange.setTextColor(item.valueChange.color)
            }
        }

        tvStatisticsDetailCardSecondValue.visible = item.secondValue.isNotEmpty()
        tvStatisticsDetailCardSecondValue.text = item.secondValue
        tvStatisticsDetailCardSecondValue.setTextSize(TypedValue.COMPLEX_UNIT_SP, item.subtitleTextSizeSp.toFloat())

        tvStatisticsDetailCardDescription.text = item.description
        tvStatisticsDetailCardDescription.setTextSize(TypedValue.COMPLEX_UNIT_SP, item.subtitleTextSizeSp.toFloat())

        if (item.icon != null) {
            cardStatisticsDetailCardIcon.visible = true
            ivStatisticsDetailCardIcon.setBackgroundResource(item.icon.iconDrawable)
            ViewCompat.setBackgroundTintList(
                ivStatisticsDetailCardIcon,
                ColorStateList.valueOf(item.icon.iconColor),
            )
        } else {
            cardStatisticsDetailCardIcon.visible = false
        }

        if (item.clickable != null) {
            root.setOnClick { onItemClick(item.clickable, root.getCoordinates()) }
        } else {
            root.isClickable = false
        }
    }
}