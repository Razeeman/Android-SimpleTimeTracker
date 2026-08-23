package com.example.util.simpletimetracker.feature_settings.views

import androidx.recyclerview.widget.RecyclerView
import com.example.util.simpletimetracker.feature_base_adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.dayOfWeek.DayOfWeekViewData
import com.example.util.simpletimetracker.feature_base_adapter.dayOfWeek.createDayOfWeekAdapterDelegate
import com.example.util.simpletimetracker.feature_settings.api.SettingsBlock
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.example.util.simpletimetracker.feature_settings.views.SettingsWeekdaysViewData as ViewData
import com.example.util.simpletimetracker.feature_settings.views.databinding.ItemSettingsWeekdaysBinding as Binding

fun createSettingsWeekdaysAdapterDelegate(
    onDayOfWeekClick: (DayOfWeekViewData) -> Unit,
) = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate,
) { binding, item, _ ->

    fun createAdapter(): BaseRecyclerAdapter {
        return BaseRecyclerAdapter(
            createDayOfWeekAdapterDelegate(onDayOfWeekClick),
        )
    }

    fun bindRecycler(
        recyclerView: RecyclerView,
        items: List<DayOfWeekViewData>,
    ) {
        recyclerView.itemAnimator = null
        recyclerView.layoutManager = FlexboxLayoutManager(binding.root.context).apply {
            flexDirection = FlexDirection.ROW
            justifyContent = JustifyContent.CENTER
            flexWrap = FlexWrap.NOWRAP
        }
        val adapter = recyclerView.adapter ?: createAdapter().also { recyclerView.adapter = it }
        (adapter as? BaseRecyclerAdapter)?.replace(items)
    }

    with(binding) {
        item as ViewData
        tvItemSettingsTitle.text = item.title
        tvItemSettingsSubtitle.text = item.subtitle
        bindRecycler(rvItemSettingsWeekdays, item.items)
    }
}

data class SettingsWeekdaysViewData(
    val block: SettingsBlock,
    val title: String,
    val subtitle: String,
    val items: List<DayOfWeekViewData>,
) : ViewHolderType {

    override fun getUniqueId(): Long = block.ordinal.toLong()

    override fun isValidType(other: ViewHolderType): Boolean = other is ViewData
}
