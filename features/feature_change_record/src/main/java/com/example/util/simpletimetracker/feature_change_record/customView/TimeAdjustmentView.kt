package com.example.util.simpletimetracker.feature_change_record.customView

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.example.util.simpletimetracker.feature_base_adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_change_record.databinding.TimeAdjustmentItemLayoutBinding
import com.example.util.simpletimetracker.feature_change_record.databinding.TimeAdjustmentViewLayoutBinding
import com.example.util.simpletimetracker.feature_views.extension.setOnClickWith
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent

class TimeAdjustmentView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(
    context,
    attrs,
    defStyleAttr
) {

    var listener: ((ViewData) -> Unit)? = null

    val adapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createAdapterDelegate(::onItemClick)
        )
    }

    private val binding: TimeAdjustmentViewLayoutBinding = TimeAdjustmentViewLayoutBinding
        .inflate(LayoutInflater.from(context), this)

    init {
        initRecycler()
        initEditMode()
    }

    private fun onItemClick(viewData: ViewData) {
        listener?.invoke(viewData)
    }

    private fun initRecycler() {
        binding.rvContainer.apply {
            layoutManager = FlexboxLayoutManager(context).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.CENTER
                flexWrap = FlexWrap.NOWRAP
            }
            adapter = this@TimeAdjustmentView.adapter
        }
    }

    private fun initEditMode() {
        if (isInEditMode) {
            List(6) {
                ViewData.Adjust(text = "Button $it", value = it.toLong())
            }.let(adapter::replace)
        }
    }

    private fun createAdapterDelegate(
        onItemClick: ((ViewData) -> Unit),
    ) = createRecyclerBindingAdapterDelegate<ViewData, TimeAdjustmentItemLayoutBinding>(
        TimeAdjustmentItemLayoutBinding::inflate
    ) { binding, item, _ ->

        with(binding) {
            item as ViewData

            root.text = item.text
            root.setOnClickWith(item, onItemClick)
        }
    }

    sealed class ViewData : ViewHolderType {
        abstract val text: String

        override fun getUniqueId(): Long = 0L
        override fun isValidType(other: ViewHolderType): Boolean = false

        data class Now(
            override val text: String,
        ) : ViewData()

        data class Adjust(
            override val text: String,
            val value: Long,
        ) : ViewData()
    }
}