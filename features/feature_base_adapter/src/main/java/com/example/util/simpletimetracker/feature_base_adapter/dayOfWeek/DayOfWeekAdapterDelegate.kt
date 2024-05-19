package com.example.util.simpletimetracker.feature_base_adapter.dayOfWeek

import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.dayOfWeek.DayOfWeekViewData.Width.MatchParent
import com.example.util.simpletimetracker.feature_base_adapter.dayOfWeek.DayOfWeekViewData.Width.WrapContent
import com.example.util.simpletimetracker.feature_views.extension.dpToPx
import com.example.util.simpletimetracker.feature_views.extension.setOnClickWith
import com.example.util.simpletimetracker.feature_base_adapter.databinding.ItemDayOfWeekBinding as Binding
import com.example.util.simpletimetracker.feature_base_adapter.dayOfWeek.DayOfWeekViewData as ViewData

fun createDayOfWeekAdapterDelegate(
    onClick: (ViewData) -> Unit,
) = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate,
) { binding, item, _ ->

    with(binding) {
        item as ViewData

        containerDayOfWeekItem.layoutParams.width = when (item.width) {
            is MatchParent -> ViewGroup.LayoutParams.MATCH_PARENT
            is WrapContent -> ViewGroup.LayoutParams.WRAP_CONTENT
        }
        cardDayOfWeekItem.setCardBackgroundColor(item.color)
        btnDayOfWeekItem.text = item.text
        btnDayOfWeekItem.setOnClickWith(item, onClick)
        btnDayOfWeekItem.updatePadding(
            left = item.paddingHorizontalDp.dpToPx(),
            right = item.paddingHorizontalDp.dpToPx(),
        )
        cardDayOfWeekItemSelected.isVisible = item.selected
    }
}
