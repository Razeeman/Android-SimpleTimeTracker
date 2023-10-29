package com.example.util.simpletimetracker.feature_base_adapter.multitaskRecord.customView

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.cardview.widget.CardView
import com.example.util.simpletimetracker.feature_base_adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.createRecyclerBindingAdapterDelegate
import com.example.util.simpletimetracker.feature_base_adapter.databinding.ItemMultitaskDataLayoutBinding
import com.example.util.simpletimetracker.feature_base_adapter.databinding.MultitaskRecordViewLayoutBinding
import com.example.util.simpletimetracker.feature_views.extension.getThemedAttr
import com.example.util.simpletimetracker.feature_views.R
import com.example.util.simpletimetracker.feature_views.viewData.RecordTypeIcon

class MultitaskRecordView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : CardView(
    context,
    attrs,
    defStyleAttr
) {

    private val adapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(createAdapterDelegate())
    }
    private val binding: MultitaskRecordViewLayoutBinding = MultitaskRecordViewLayoutBinding
        .inflate(LayoutInflater.from(context), this)

    init {
        initProps()
        initRecycler()
        initEditMode()
    }

    fun setData(data: ViewData) {
        adapter.replaceAsNew(data.items)
        binding.tvRecordItemTimeStarted.text = data.timeStarted
        binding.tvRecordItemTimeFinished.text = data.timeFinished
        binding.tvMultitaskRecordItemDuration.text = data.duration
    }

    private fun initProps() {
        context.getThemedAttr(R.attr.appCardBackgroundColor).let(::setCardBackgroundColor)
        radius = resources.getDimensionPixelOffset(R.dimen.record_type_card_corner_radius).toFloat()
        // TODO doesn't work here for some reason, need to set in the layout
        cardElevation = resources.getDimensionPixelOffset(R.dimen.record_type_card_elevation).toFloat()
        preventCornerOverlap = false
        useCompatPadding = true
    }

    private fun initRecycler() {
        binding.rvMultitaskRecordItem.itemAnimator = null
        binding.rvMultitaskRecordItem.adapter = this.adapter
    }

    private fun initEditMode() {
        if (isInEditMode) {
            val record = ItemViewData(
                name = "Record",
                tagName = "Tag",
                iconId = RecordTypeIcon.Image(0),
                color = Color.RED,
                comment = "Comment",
            )
            val items = listOf(
                record.copy(name = "Record 1", color = Color.RED),
                record.copy(name = "Record 2", color = Color.BLUE),
                record.copy(name = "Record 3", color = Color.GREEN),
            )
            val data = ViewData(
                timeStarted = "07:35",
                timeFinished = "11:58",
                duration = "5h 23m 3s",
                items = items,
            )
            setData(data)
        }
    }

    private fun createAdapterDelegate() =
        createRecyclerBindingAdapterDelegate<ItemViewData, ItemMultitaskDataLayoutBinding>(
            ItemMultitaskDataLayoutBinding::inflate
        ) { binding, item, _ ->

            with(binding.root) {
                item as ItemViewData

                itemName = item.name
                itemTagName = item.tagName
                itemIcon = item.iconId
                itemColor = item.color
                itemComment = item.comment
            }
        }

    data class ViewData(
        val timeStarted: String,
        val timeFinished: String,
        val duration: String,
        val items: List<ItemViewData>,
    )

    data class ItemViewData(
        val name: String,
        val tagName: String,
        val iconId: RecordTypeIcon,
        val color: Int,
        val comment: String
    ) : ViewHolderType {

        override fun getUniqueId(): Long = name.hashCode().toLong()

        override fun isValidType(other: ViewHolderType): Boolean = other is ItemViewData
    }
}