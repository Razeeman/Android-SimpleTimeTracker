package com.example.util.simpletimetracker.feature_complex_rules.adapter

import androidx.annotation.ColorInt
import androidx.recyclerview.widget.RecyclerView
import com.example.util.simpletimetracker.feature_base_adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_views.ColorUtils
import com.example.util.simpletimetracker.feature_views.extension.setOnClickWith
import com.example.util.simpletimetracker.feature_views.extension.visible
import com.example.util.simpletimetracker.feature_views.viewData.RecordTypeIcon
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.example.util.simpletimetracker.feature_complex_rules.adapter.ComplexRuleElementContentViewData as ContentViewData
import com.example.util.simpletimetracker.feature_complex_rules.adapter.ComplexRuleElementTitleViewData as TitleViewData
import com.example.util.simpletimetracker.feature_complex_rules.adapter.ComplexRuleViewData as ViewData
import com.example.util.simpletimetracker.feature_complex_rules.databinding.ItemComplexRuleElementContentBinding as ContentBinding
import com.example.util.simpletimetracker.feature_complex_rules.databinding.ItemComplexRuleElementTitleBinding as TitleBinding
import com.example.util.simpletimetracker.feature_complex_rules.databinding.ItemComplexRuleLayoutBinding as Binding

fun createComplexRuleAdapterDelegate(
    onItemClick: ((ViewData) -> Unit),
    onDisableClick: ((ViewData) -> Unit),
) = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate,
) { binding, item, _ ->

    fun createAdapter(): BaseRecyclerAdapter {
        return BaseRecyclerAdapter(
            createElementTitleAdapter(),
            createElementContentAdapter(),
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
        val adapter: RecyclerView.Adapter<*> = recyclerView.adapter
            ?: createAdapter().also { recyclerView.adapter = it }
        (adapter as? BaseRecyclerAdapter)?.replace(items)
    }

    with(binding) {
        item as ViewData

        bindRecycler(rvComplexRuleItemActions, item.actionItems)
        bindRecycler(rvComplexRuleItemConditions, item.conditionItems)

        containerComplexRuleItem.setCardBackgroundColor(item.color)
        btnComplexRuleButtonDisable.setCardBackgroundColor(item.disableButtonColor)
        tvComplexRuleButtonDisable.text = item.disableButtonText
        viewComplexRuleItemDivider.setBackgroundColor(ColorUtils.normalizeLightness(item.color))

        viewComplexRuleItemConditionsClick.setOnClickWith(item, onItemClick)
        btnComplexRuleButtonDisable.setOnClickWith(item, onDisableClick)
    }
}

data class ComplexRuleViewData(
    val id: Long,
    val actionItems: List<ViewHolderType>,
    val conditionItems: List<ViewHolderType>,
    @ColorInt val color: Int,
    @ColorInt val disableButtonColor: Int,
    val disableButtonText: String,
) : ViewHolderType {

    override fun getUniqueId(): Long = id

    override fun isValidType(other: ViewHolderType): Boolean = other is ViewData
}

data class ComplexRuleElementTitleViewData(
    val text: String,
) : ViewHolderType {

    override fun getUniqueId(): Long = text.hashCode().toLong()

    override fun isValidType(other: ViewHolderType): Boolean = other is TitleViewData
}

data class ComplexRuleElementContentViewData(
    val text: String,
    val icon: RecordTypeIcon?,
    @ColorInt val color: Int,
) : ViewHolderType {

    override fun getUniqueId(): Long = text.hashCode().toLong()

    override fun isValidType(other: ViewHolderType): Boolean = other is ContentViewData
}

private fun createElementTitleAdapter() = createRecyclerBindingAdapterDelegate<TitleViewData, TitleBinding>(
    TitleBinding::inflate,
) { binding, item, _ ->
    with(binding) {
        item as TitleViewData

        (binding.root.layoutParams as? FlexboxLayoutManager.LayoutParams)
            ?.apply { isWrapBefore = true }
        tvComplexRuleElementItemTitle.text = item.text
    }
}

private fun createElementContentAdapter() = createRecyclerBindingAdapterDelegate<ContentViewData, ContentBinding>(
    ContentBinding::inflate,
) { binding, item, _ ->
    with(binding) {
        item as ContentViewData

        tvComplexRuleElementItemContent.text = item.text
        cvComplexRuleElementItemContent.setCardBackgroundColor(item.color)
        if (item.icon != null) {
            ivComplexRuleElementItemContent.visible = true
            ivComplexRuleElementItemContent.itemIcon = item.icon
        } else {
            ivComplexRuleElementItemContent.visible = false
        }
    }
}
