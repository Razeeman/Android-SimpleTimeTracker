package com.example.util.simpletimetracker.feature_change_goals.views

import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.feature_change_goals.views.GoalsFooterViewData as ViewData
import com.example.util.simpletimetracker.feature_change_goals.views.databinding.ChangeGoalsFooterItemBinding as Binding

fun createGoalsFooterAdapterDelegate(
    onGoalAdd: () -> Unit,
) = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate,
) { binding, _, _ ->

    // TODO GOAL use common button
    binding.root.setOnClick(onGoalAdd)
}

data object GoalsFooterViewData : ViewHolderType {

    override fun getUniqueId(): Long = 0L

    override fun isValidType(other: ViewHolderType): Boolean = other is ViewData
}