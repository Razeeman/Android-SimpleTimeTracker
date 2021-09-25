package com.example.util.simpletimetracker.feature_statistics_detail.adapter

import android.content.res.ColorStateList
import android.util.TypedValue
import androidx.core.view.ViewCompat
import com.example.util.simpletimetracker.core.adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.feature_views.extension.visible
import com.example.util.simpletimetracker.feature_statistics_detail.databinding.StatisticsDetailCardItemBinding as Binding
import com.example.util.simpletimetracker.feature_statistics_detail.viewData.StatisticsDetailCardViewData as ViewData

fun createStatisticsDetailCardAdapterDelegate(
    titleTextSize: Int,
    onItemClick: () -> Unit
) = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate
) { binding, item, _ ->

    with(binding) {
        item as ViewData

        tvStatisticsDetailCardValue.text = item.title
        tvStatisticsDetailCardValue.setTextSize(TypedValue.COMPLEX_UNIT_PX, titleTextSize.toFloat())
        tvStatisticsDetailCardDescription.text = item.subtitle

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