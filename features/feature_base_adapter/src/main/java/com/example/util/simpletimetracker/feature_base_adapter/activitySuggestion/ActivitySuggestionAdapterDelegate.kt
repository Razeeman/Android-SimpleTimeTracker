package com.example.util.simpletimetracker.feature_base_adapter.activitySuggestion

import androidx.annotation.ColorInt
import androidx.recyclerview.widget.RecyclerView
import com.example.util.simpletimetracker.feature_base_adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.listElement.createListElementAdapter
import com.example.util.simpletimetracker.feature_views.ColorUtils
import com.example.util.simpletimetracker.feature_views.extension.setOnClickWith
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.example.util.simpletimetracker.feature_base_adapter.activitySuggestion.ActivitySuggestionViewData as ViewData
import com.example.util.simpletimetracker.feature_base_adapter.databinding.ItemActivitySuggestionLayoutBinding as Binding

fun createActivitySuggestionAdapterDelegate(
    onItemClick: ((ViewData) -> Unit),
) = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate,
) { binding, item, _ ->

    fun createAdapter(): BaseRecyclerAdapter {
        return BaseRecyclerAdapter(
            createListElementAdapter(),
        )
    }

    fun bindRecycler(
        recyclerView: RecyclerView,
        items: List<ViewHolderType>,
    ) {
        recyclerView.itemAnimator = null
        recyclerView.layoutManager = FlexboxLayoutManager(binding.root.context).apply {
            flexDirection = FlexDirection.ROW
            justifyContent = JustifyContent.FLEX_START
            flexWrap = FlexWrap.WRAP
        }
        val adapter = recyclerView.adapter ?: createAdapter().also { recyclerView.adapter = it }
        (adapter as? BaseRecyclerAdapter)?.replace(items)
    }

    with(binding) {
        item as ViewData

        bindRecycler(rvActivitySuggestionItemActions, item.activity)
        bindRecycler(rvActivitySuggestionItemConditions, item.suggestions)

        containerActivitySuggestionItem.setCardBackgroundColor(item.color)
        viewActivitySuggestionItemDivider.setBackgroundColor(ColorUtils.normalizeLightness(item.color))

        viewActivitySuggestionItemConditionsClick.setOnClickWith(item, onItemClick)
    }
}

data class ActivitySuggestionViewData(
    val id: Long,
    val activity: List<ViewHolderType>,
    val suggestions: List<ViewHolderType>,
    @ColorInt val color: Int,
) : ViewHolderType {

    override fun getUniqueId(): Long = id

    override fun isValidType(other: ViewHolderType): Boolean = other is ViewData
}
