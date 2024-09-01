package com.example.util.simpletimetracker.core.view.timeAdjustment

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.view.updatePadding
import com.example.util.simpletimetracker.core.databinding.TimeAdjustmentItemLayoutBinding
import com.example.util.simpletimetracker.core.databinding.TimeAdjustmentViewLayoutBinding
import com.example.util.simpletimetracker.feature_base_adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_views.extension.dpToPx
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
    defStyleAttr,
) {

    var listener: ((ViewData) -> Unit)? = null

    val adapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createAdapterDelegate(::onItemClick),
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
            itemAnimator = null
        }
    }

    private fun initEditMode() {
        if (isInEditMode) {
            List(6) {
                ViewData.Adjust(text = "Btn $it", value = it.toLong())
            }.let(adapter::replace)
        }
    }

    private fun createAdapterDelegate(
        onItemClick: ((ViewData) -> Unit),
    ) = createRecyclerBindingAdapterDelegate<ViewData, TimeAdjustmentItemLayoutBinding>(
        TimeAdjustmentItemLayoutBinding::inflate,
    ) { binding, item, _ ->

        with(binding) {
            item as ViewData

            tvTimeAdjustmentItem.text = item.text
            root.setOnClickWith(item, onItemClick)
            val padding = (if (item is ViewData.Now) 12 else 4).dpToPx()
            tvTimeAdjustmentItem.updatePadding(left = padding, right = padding)

            (root.layoutParams as? FlexboxLayoutManager.LayoutParams)?.apply {
                flexGrow = if (item is ViewData.Now) 0f else 1f
            }
        }
    }

    sealed interface ViewData : ViewHolderType {
        val text: String

        override fun getUniqueId(): Long = text.hashCode().toLong()
        override fun isValidType(other: ViewHolderType): Boolean = false

        data class Now(
            override val text: String,
        ) : ViewData

        data class Zero(
            override val text: String,
        ): ViewData

        data class Adjust(
            override val text: String,
            val value: Long,
        ) : ViewData
    }
}