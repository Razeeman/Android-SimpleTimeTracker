package com.example.util.simpletimetracker.feature_statistics_detail.adapter

import android.content.res.ColorStateList
import android.graphics.Typeface
import android.util.TypedValue
import androidx.core.view.ViewCompat
import com.example.util.simpletimetracker.core.extension.getThemedAttr
import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_statistics_detail.R
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.feature_views.extension.visible
import com.example.util.simpletimetracker.feature_statistics_detail.databinding.StatisticsDetailCardItemBinding as Binding
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailCardViewData as ViewData

fun createStatisticsDetailCardAdapterDelegate(
    titleTextSize: Int,
    subtitleTextSize: Int,
    onItemClick: () -> Unit
) = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate
) { binding, item, _ ->

    with(binding) {
        item as ViewData

        tvStatisticsDetailCardValue.text = item.value
        tvStatisticsDetailCardValue.setTextSize(TypedValue.COMPLEX_UNIT_PX, titleTextSize.toFloat())
        if (item.accented) {
            tvStatisticsDetailCardValue.typeface =
                Typeface.create("sans-serif-condensed", Typeface.BOLD)
            tvStatisticsDetailCardValue
                .setTextColor(root.context.getThemedAttr(R.attr.colorSecondary))
            tvStatisticsDetailCardValue.letterSpacing = -0.05f
        }

        tvStatisticsDetailCardSecondValue.visible = item.secondValue.isNotEmpty()
        tvStatisticsDetailCardSecondValue.text = item.secondValue
        tvStatisticsDetailCardSecondValue.setTextSize(TypedValue.COMPLEX_UNIT_PX, subtitleTextSize.toFloat())

        tvStatisticsDetailCardDescription.text = item.description
        tvStatisticsDetailCardDescription.setTextSize(TypedValue.COMPLEX_UNIT_PX, subtitleTextSize.toFloat())

        if (item.icon != null) {
            cardStatisticsDetailCardIcon.visible = true
            ivStatisticsDetailCardIcon.setBackgroundResource(item.icon.iconDrawable)
            ViewCompat.setBackgroundTintList(
                ivStatisticsDetailCardIcon,
                ColorStateList.valueOf(item.icon.iconColor)
            )
            root.setOnClick(onItemClick)
        } else {
            cardStatisticsDetailCardIcon.visible = false
            root.isClickable = false
        }
    }
}