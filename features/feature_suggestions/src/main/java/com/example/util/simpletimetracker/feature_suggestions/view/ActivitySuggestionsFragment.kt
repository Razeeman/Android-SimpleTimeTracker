package com.example.util.simpletimetracker.feature_suggestions.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.example.util.simpletimetracker.core.base.BaseFragment
import com.example.util.simpletimetracker.feature_dialogs.api.TypesSelectionDialogListener
import com.example.util.simpletimetracker.core.extension.onItemMoved
import com.example.util.simpletimetracker.core.utils.ITEM_ALPHA_DEFAULT
import com.example.util.simpletimetracker.core.utils.ITEM_ALPHA_SELECTED
import com.example.util.simpletimetracker.core.utils.ITEM_SCALE_DEFAULT
import com.example.util.simpletimetracker.core.utils.ITEM_SCALE_SELECTED
import com.example.util.simpletimetracker.core.utils.InsetConfiguration
import com.example.util.simpletimetracker.domain.extension.orFalse
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.record.model.RecordBase
import com.example.util.simpletimetracker.feature_base_adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.button.createButtonAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.divider.createDividerAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.emptySpace.createEmptySpaceAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.hint.createHintAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.loader.createLoaderAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.recordType.createRecordTypeAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.recordTypeRelation.ActivitySuggestionListViewData
import com.example.util.simpletimetracker.feature_base_adapter.recordTypeRelation.createActivitySuggestionListAdapterDelegate
import com.example.util.simpletimetracker.feature_suggestions.adapter.createActivitySuggestionSpecialAdapterDelegate
import com.example.util.simpletimetracker.feature_suggestions.viewModel.ActivitySuggestionsViewModel
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import dagger.hilt.android.AndroidEntryPoint
import com.example.util.simpletimetracker.feature_suggestions.databinding.ActivitySuggestionsFragmentBinding as Binding

@AndroidEntryPoint
class ActivitySuggestionsFragment :
    BaseFragment<Binding>(),
    TypesSelectionDialogListener {

    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> Binding =
        Binding::inflate

    override var insetConfiguration: InsetConfiguration =
        InsetConfiguration.ApplyToView { binding.root }

    private val viewModel: ActivitySuggestionsViewModel by viewModels()

    private val viewDataAdapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createDividerAdapterDelegate(),
            createEmptySpaceAdapterDelegate(),
            createLoaderAdapterDelegate(),
            createHintAdapterDelegate(),
            createRecordTypeAdapterDelegate(),
            createActivitySuggestionListAdapterDelegate(),
            createActivitySuggestionSpecialAdapterDelegate(throttle(viewModel::onSpecialSuggestionClick)),
            createButtonAdapterDelegate(throttle(viewModel::onItemButtonClick)),
        )
    }

    override fun initUi(): Unit = with(binding) {
        rvActivitySuggestionsList.apply {
            layoutManager = FlexboxLayoutManager(requireContext()).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.FLEX_START
                flexWrap = FlexWrap.WRAP
            }
            adapter = viewDataAdapter
        }
    }

    override fun initUx() = with(binding) {
        btnActivitySuggestionsSave.setOnClick(throttle(viewModel::onSaveClick))
        initOnItemMoved()
    }

    override fun initViewModel(): Unit = with(viewModel) {
        viewData.observe(viewDataAdapter::replace)
    }

    override fun onDataSelected(
        tag: String,
        dataIds: List<Long>,
        tagValues: List<RecordBase.Tag>,
        selectValueOnStartTagIds: List<Long>,
    ) {
        viewModel.onTypesSelected(dataIds, tag)
    }

    private fun initOnItemMoved() = with(binding) {
        fun ViewHolderType.isSelectable(): Boolean {
            return this is ActivitySuggestionListViewData
        }

        rvActivitySuggestionsList.onItemMoved(
            getIsSelectable = { viewHolder ->
                viewHolder?.adapterPosition
                    ?.let { viewDataAdapter.getItemByPosition(it) }
                    ?.isSelectable()
                    .orFalse()
            },
            getSelectablePositions = { viewHolder ->
                val items = viewDataAdapter.currentList
                val itemPosition = viewHolder?.adapterPosition.orZero()

                val from = items.indexOfLast {
                    val index = items.indexOf(it)
                    val prevItem = items.getOrNull(index - 1)
                    it.isSelectable() &&
                        index <= itemPosition &&
                        (prevItem != null && !prevItem.isSelectable())
                }
                val to = items.indexOfFirst {
                    val index = items.indexOf(it)
                    val nextItem = items.getOrNull(index + 1)
                    it.isSelectable() &&
                        index >= itemPosition &&
                        (nextItem != null && !nextItem.isSelectable())
                }
                from to to
            },
            onSelected = ::setItemSelected,
            onClear = ::setItemUnselected,
            onMoved = { list, _, to ->
                viewModel.onItemMoved(
                    items = list,
                    toPosition = to,
                )
            },
        )
    }

    private fun setItemSelected(viewHolder: RecyclerView.ViewHolder?) = viewHolder?.run {
        itemView.alpha = ITEM_ALPHA_SELECTED
        itemView.scaleX = ITEM_SCALE_SELECTED
        itemView.scaleY = ITEM_SCALE_SELECTED
    } ?: Unit

    private fun setItemUnselected(viewHolder: RecyclerView.ViewHolder) = viewHolder.run {
        itemView.alpha = ITEM_ALPHA_DEFAULT
        itemView.scaleX = ITEM_SCALE_DEFAULT
        itemView.scaleY = ITEM_SCALE_DEFAULT
    }
}
