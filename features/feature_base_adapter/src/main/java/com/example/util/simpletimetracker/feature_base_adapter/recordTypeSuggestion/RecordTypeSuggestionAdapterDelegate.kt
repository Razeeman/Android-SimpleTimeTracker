package com.example.util.simpletimetracker.feature_base_adapter.recordTypeSuggestion

import android.view.ViewGroup
import com.example.util.simpletimetracker.feature_base_adapter.BaseRecyclerBindingViewHolder
import com.example.util.simpletimetracker.feature_base_adapter.RecyclerAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.recordType.createRecordTypeAdapterDelegate
import com.example.util.simpletimetracker.feature_views.TransitionNames
import com.example.util.simpletimetracker.feature_views.extension.layoutInflater
import com.example.util.simpletimetracker.feature_base_adapter.databinding.ItemRecordTypeLayoutBinding as BaseBinding
import com.example.util.simpletimetracker.feature_base_adapter.recordType.RecordTypeViewData as BaseViewData
import com.example.util.simpletimetracker.feature_base_adapter.recordTypeSuggestion.RecordTypeSuggestionViewData as ViewData

// Wrapper around RecordType delegate.
// Passes all calls to other delegate.
fun createRecordTypeSuggestionAdapterDelegate(
    type: ViewData.Type,
    onItemClick: ((BaseViewData) -> Unit)? = null,
    onItemClickWithData: ((BaseViewData, Pair<Any, String>) -> Unit)? = null,
    onItemLongClick: ((BaseViewData, Pair<Any, String>) -> Unit)? = null,
    withTransition: Boolean = true,
): RecyclerAdapterDelegate {
    val baseAdapter = createRecordTypeAdapterDelegate(
        onItemClick = onItemClick,
        onItemClickWithData = onItemClickWithData,
        onItemLongClick = onItemLongClick,
        withTransition = withTransition,
        transitionNamePrefix = TransitionNames.RECORD_TYPE_SUGGESTION,
    )

    return object : RecyclerAdapterDelegate {

        override fun isForValidType(check: ViewHolderType): Boolean {
            return check is ViewData && check.type == type
        }

        override fun onCreateViewHolder(parent: ViewGroup): BaseRecyclerBindingViewHolder<BaseBinding> {
            val baseViewHolder = baseAdapter.onCreateViewHolder(parent)
                as? BaseRecyclerBindingViewHolder<*>
            // Just in case, so it wouldn't crash.
            // Worst case - it would show unbound layout.
            val fallbackBinding by lazy {
                BaseBinding.inflate(parent.layoutInflater, parent, false)
            }

            return BaseRecyclerBindingViewHolder(
                binding = baseViewHolder?.binding as? BaseBinding ?: fallbackBinding,
                onBind = { binding, item, payload ->
                    binding.root.tag = ViewData.TEST_TAG
                    item as ViewData
                    baseViewHolder?.bind(item.data, payload)
                },
            )
        }

        override fun getViewHolderTypeName(): String = ViewData::class.java.simpleName
    }
}