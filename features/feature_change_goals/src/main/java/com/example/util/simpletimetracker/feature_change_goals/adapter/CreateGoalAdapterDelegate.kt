package com.example.util.simpletimetracker.feature_change_goals.adapter

import android.graphics.Rect
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import com.example.util.simpletimetracker.feature_base_adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.dayOfWeek.createDayOfWeekAdapterDelegate
import com.example.util.simpletimetracker.feature_change_goals.api.ChangeRecordTypeGoalsViewData
import com.example.util.simpletimetracker.feature_change_goals.api.GoalsViewModelDelegate
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.example.util.simpletimetracker.feature_change_goals.api.ChangeRecordTypeGoalsViewData.GoalViewData as ViewData
import com.example.util.simpletimetracker.feature_change_goals.databinding.ChangeGoalLayoutBinding as Binding

fun createGoalAdapterDelegate(
    viewModel: GoalsViewModelDelegate,
) = createRecyclerBindingAdapterDelegate<ViewData, Binding>(
    Binding::inflate,
) { binding, item, _ ->
    item as ViewData
    val holderState = (binding.root.tag as? GoalHolderState) ?: createHolderState(
        binding = binding,
        viewModel = viewModel,
    ).also { binding.root.tag = it }

    holderState.key = item.key
    holderState.isBinding = true

    binding.tvChangeRecordTypeGoalSummary.text = item.summary
    binding.containerChangeRecordTypeGoalEditor.isVisible = item.isExpanded
    binding.arrowChangeRecordTypeGoalSummary.rotation = if (item.isExpanded) 180f else 0f

    binding.spinnerRecordTypeGoalRange.setData(
        items = item.rangeItems,
        selectedPosition = item.rangeSelectedPosition,
    )
    binding.tvChangeRecordTypeGoalRange.text = item.rangeItems
        .getOrNull(item.rangeSelectedPosition)?.text.orEmpty()
    binding.spinnerRecordTypeGoalType.setData(
        items = item.typeItems,
        selectedPosition = item.typeSelectedPosition,
    )
    binding.tvChangeRecordTypeGoalType.text = item.typeItems
        .getOrNull(item.typeSelectedPosition)?.text.orEmpty()
    val typeSelectable = item.typeItems.size > 1
    binding.fieldRecordTypeGoalType.isEnabled = typeSelectable
    binding.arrowChangeRecordTypeGoalType.isVisible = typeSelectable

    when (item.type) {
        is ChangeRecordTypeGoalsViewData.Type.Duration -> {
            binding.tvChangeRecordTypeGoalDurationValue.text = item.value
            binding.fieldChangeRecordTypeGoalDuration.isVisible = true
            binding.inputChangeRecordTypeGoalCount.isInvisible = true
        }
        is ChangeRecordTypeGoalsViewData.Type.Count -> {
            val editText = binding.etChangeRecordTypeGoalCountValue
            if (editText.text.toString() != item.value) {
                val selection = editText.selectionStart.coerceAtLeast(0)
                editText.setText(item.value)
                editText.setSelection(selection.coerceAtMost(item.value.length))
            }
            binding.fieldChangeRecordTypeGoalDuration.isInvisible = true
            binding.inputChangeRecordTypeGoalCount.isVisible = true
        }
    }

    binding.rvChangeRecordTypeGoalDays.isVisible = item.daysOfWeek.isNotEmpty()
    holderState.dayOfWeekAdapter?.replace(item.daysOfWeek)

    binding.btnChangeRecordTypeGoalSubtype.isVisible = item.subtypeItems.isNotEmpty()
    if (item.subtypeItems.isNotEmpty()) {
        binding.btnChangeRecordTypeGoalSubtype.replace(item.subtypeItems)
    }

    binding.spinnerRecordTypeGoalRange.setProcessSameItemSelection(false)
    binding.spinnerRecordTypeGoalType.setProcessSameItemSelection(false)
    binding.fieldRecordTypeGoalRange.setOnClick(binding.spinnerRecordTypeGoalRange::performClick)
    binding.fieldRecordTypeGoalType.setOnClick(binding.spinnerRecordTypeGoalType::performClick)
    binding.spinnerRecordTypeGoalRange.onPositionSelected = { viewModel.onGoalRangeSelected(item.key, it) }
    binding.spinnerRecordTypeGoalType.onPositionSelected = { viewModel.onGoalTypeSelected(item.key, it) }
    binding.fieldChangeRecordTypeGoalDuration.setOnClick { viewModel.onGoalTimeClick(item.key) }
    binding.btnChangeRecordTypeGoalSubtype.listener = { viewModel.onGoalSubTypeSelected(item.key, it) }
    binding.btnChangeRecordTypeGoalDelete.setOnClick { viewModel.onGoalRemove(item.key) }
    binding.containerChangeRecordTypeGoalSummary.setOnClick { viewModel.onGoalToggle(item.key) }

    holderState.isBinding = false

    // TODO GOAL maybe scroll to botton so that the Add button will be visible?
    if (item.requestScroll && holderState.scrollRequestedForKey != item.key) {
        holderState.scrollRequestedForKey = item.key
        binding.root.post {
            val rect = Rect(0, 0, binding.root.width, binding.root.height)
            binding.root.requestRectangleOnScreen(rect, false)
            viewModel.onGoalScrollHandled(item.key)
        }
    }
}

// Initializes resources that must be created only once per holder,
// such as the text watcher and recycler adapter.
// The local state variable lets their callbacks access the
// current holder key after the state is initialized.
private fun createHolderState(
    binding: Binding,
    viewModel: GoalsViewModelDelegate,
): GoalHolderState {
    val state = GoalHolderState(
        key = 0L,
        dayOfWeekAdapter = null,
        isBinding = false,
        scrollRequestedForKey = null,
    )

    val dayOfWeekAdapter = BaseRecyclerAdapter(
        createDayOfWeekAdapterDelegate(
            onClick = { viewModel.onDayOfWeekClick(state.key, it) },
        ),
    )
    state.dayOfWeekAdapter = dayOfWeekAdapter
    binding.rvChangeRecordTypeGoalDays.apply {
        itemAnimator = null
        layoutManager = FlexboxLayoutManager(context).apply {
            flexDirection = FlexDirection.ROW
            justifyContent = JustifyContent.CENTER
            flexWrap = FlexWrap.NOWRAP
        }
        adapter = dayOfWeekAdapter
    }

    binding.etChangeRecordTypeGoalCountValue.doAfterTextChanged {
        if (!state.isBinding) viewModel.onGoalCountChange(state.key, it.toString())
    }

    return state
}

// Stores holder-scoped data that must persist across binds and remain available to callbacks.
private data class GoalHolderState(
    var key: Long,
    var dayOfWeekAdapter: BaseRecyclerAdapter?,
    var isBinding: Boolean,
    var scrollRequestedForKey: Long?,
)