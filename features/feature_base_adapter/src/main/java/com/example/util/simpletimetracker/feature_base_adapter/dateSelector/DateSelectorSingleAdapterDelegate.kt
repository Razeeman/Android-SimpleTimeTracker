package com.example.util.simpletimetracker.feature_base_adapter.dateSelector

import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import com.example.util.simpletimetracker.feature_base_adapter.InfiniteRecyclerAdapter
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_views.extension.setOnClickWith
import com.example.util.simpletimetracker.feature_base_adapter.databinding.ItemDateDaySelectorBinding as Binding
import com.example.util.simpletimetracker.feature_base_adapter.dateSelector.DateSelectorSingleViewData as ViewData

fun createDateSelectorSingleAdapterDelegate(
    onItemClick: ((ViewData) -> Unit),
) = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate,
) { binding, item, _ ->

    with(binding) {
        item as ViewData

        root.updateLayoutParams {
            width = ViewGroup.LayoutParams.MATCH_PARENT
        }
        setTestTag(root, item)
        setAdditionalHint(
            dayMonth = item.dayMonth,
            additionalText = tvDateSelectorAdditionalHint,
        )
        setDayMoth(
            dayMonth = item.dayMonth,
            topText = tvDateSelectorTopText,
            bottomText = tvDateSelectorBottomText,
            increasedTextSize = item.cardData.increasedTextSize,
        )
        root.setCardData(
            cardData = item.cardData,
            viewSelected = viewDateSelectorBackgroundSelected,
            viewToday = viewDateSelectorBackgroundToday,
            viewClickable = viewDateSelectorClickable,
            textViews = listOf(
                tvDateSelectorTopText,
                tvDateSelectorBottomText,
            ),
        )

        root.setOnClickWith(item, onItemClick)
    }
}

data class DateSelectorSingleViewData(
    override val position: Int,
    val dayMonth: DateSelectorDayViewData.DayMonth,
    val cardData: DateSelectorDayViewData.CardData,
) : InfiniteRecyclerAdapter.Data {

    override fun isValidType(other: ViewHolderType): Boolean = other is ViewData
}
