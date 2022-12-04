package com.example.util.simpletimetracker.feature_base_adapter.activityFilter

import com.example.util.simpletimetracker.feature_base_adapter.activityFilter.ActivityFilterViewData as ViewData
import com.example.util.simpletimetracker.feature_base_adapter.databinding.ItemActivityFilterLayoutBinding as Binding
import androidx.core.view.ViewCompat
import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_views.TransitionNames
import com.example.util.simpletimetracker.feature_views.extension.setOnClickWith
import com.example.util.simpletimetracker.feature_views.extension.setOnLongClick

fun createActivityFilterAdapterDelegate(
    onClick: ((ViewData) -> Unit),
    onLongClick: ((ViewData, Pair<Any, String>) -> Unit) = { _, _ -> },
) = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate
) { binding, item, _ ->

    with(binding.viewActivityFilterItem) {
        item as ViewData

        val transitionName = TransitionNames.ACTIVITY_FILTER + item.id
        val sharedElements: Pair<Any, String> = this to transitionName
        ViewCompat.setTransitionName(this, transitionName)

        itemColor = item.color
        itemName = item.name

        setOnClickWith(item, onClick)
        setOnLongClick { onLongClick(item, sharedElements) }
    }
}