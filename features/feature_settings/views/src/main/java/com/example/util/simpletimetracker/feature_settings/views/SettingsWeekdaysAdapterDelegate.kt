package com.example.util.simpletimetracker.feature_settings.views

import androidx.recyclerview.widget.RecyclerView
import com.example.util.simpletimetracker.feature_base_adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.dayOfWeek.DayOfWeekViewData
import com.example.util.simpletimetracker.feature_base_adapter.dayOfWeek.createDayOfWeekAdapterDelegate
import com.example.util.simpletimetracker.feature_settings.api.SettingsBlock
import com.example.util.simpletimetracker.feature_views.extension.visible
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.example.util.simpletimetracker.feature_settings.views.SettingsWeekdaysViewData as ViewData
import com.example.util.simpletimetracker.feature_settings.views.databinding.ItemSettingsWeekdaysBinding as Binding

fun createSettingsWeekdaysAdapterDelegate(
    onDayOfWeekClick: (SettingsBlock, DayOfWeekViewData) -> Unit,
) = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate,
) { binding, item, _ ->

    fun createAdapter(
        onClick: (DayOfWeekViewData) -> Unit,
    ): BaseRecyclerAdapter {
        return BaseRecyclerAdapter(
            createDayOfWeekAdapterDelegate(onClick = { onClick(it) }),
        )
    }

    fun bindRecycler(
        recyclerView: RecyclerView,
        viewData: ViewData,
        onClick: (DayOfWeekViewData) -> Unit,
    ) {
        recyclerView.itemAnimator = null
        recyclerView.layoutManager = FlexboxLayoutManager(binding.root.context).apply {
            flexDirection = FlexDirection.ROW
            justifyContent = JustifyContent.CENTER
            flexWrap = FlexWrap.NOWRAP
        }
        val adapter = recyclerView.adapter ?: createAdapter(onClick).also { recyclerView.adapter = it }
        (adapter as? BaseRecyclerAdapter)?.replace(viewData.items)
    }

    with(binding) {
        item as ViewData
        tvItemSettingsTitle.text = item.title
        // TODO add extension bindOptional
        if (item.subtitle.isEmpty()) {
            tvItemSettingsSubtitle.visible = false
        } else {
            tvItemSettingsSubtitle.text = item.subtitle
            tvItemSettingsSubtitle.visible = true
        }
        // Needed for tests and for clicks receiving correct block after bind / rebind.
        rvItemSettingsWeekdays.tag = item.block
        bindRecycler(
            recyclerView = rvItemSettingsWeekdays,
            viewData = item,
            onClick = { dayOfWeek ->
                // Need actual bound block, because adapter will be created only once
                // and not recreated on rebind.
                val currentlyBoundBlock = rvItemSettingsWeekdays.tag as? SettingsBlock
                currentlyBoundBlock?.let { block -> onDayOfWeekClick(block, dayOfWeek) }
            },
        )
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
