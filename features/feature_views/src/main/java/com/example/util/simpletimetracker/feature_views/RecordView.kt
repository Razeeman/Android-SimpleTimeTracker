package com.example.util.simpletimetracker.feature_views

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.example.util.simpletimetracker.feature_views.databinding.RecordViewLayoutBinding
import com.example.util.simpletimetracker.feature_views.extension.setForegroundSpan
import com.example.util.simpletimetracker.feature_views.extension.toSpannableString
import com.example.util.simpletimetracker.feature_views.extension.visible
import com.example.util.simpletimetracker.feature_views.viewData.RecordTypeIcon

class RecordView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : CardView(
    context,
    attrs,
    defStyleAttr,
) {

    private val binding: RecordViewLayoutBinding = RecordViewLayoutBinding
        .inflate(LayoutInflater.from(context), this)

    var itemName: String = ""
        set(value) {
            field = value
            setItemName()
        }

    var itemTagName: String = ""
        set(value) {
            field = value
            setItemName()
        }

    var itemColor: Int = Color.BLACK
        set(value) {
            setCardBackgroundColor(value)
            field = value
        }

    var itemTagColor: Int = Color.WHITE
        set(value) {
            field = value
            setItemName()
        }

    var itemIcon: RecordTypeIcon = RecordTypeIcon.Image(0)
        set(value) {
            binding.ivRecordItemIcon.itemIcon = value
            field = value
        }

    var itemTimeStarted: String = ""
        set(value) {
            binding.tvRecordItemTimeStarted.text = value
            binding.tvRecordItemTimeStarted.visible = value.isNotEmpty()
            field = value
            updateSeparatorVisibility()
        }

    var itemTimeEnded: String = ""
        set(value) {
            binding.tvRecordItemTimeFinished.text = value
            binding.tvRecordItemTimeFinished.visible = value.isNotEmpty()
            field = value
            updateSeparatorVisibility()
        }

    var itemDuration: String = ""
        set(value) {
            binding.tvRecordItemDuration.text = value
            field = value
        }

    var itemComment: String = ""
        set(value) {
            binding.tvRecordItemComment.text = value
            binding.tvRecordItemComment.visible = value.isNotEmpty()
            field = value
        }

    init {
        initProps()
        initAttrs(context, attrs, defStyleAttr)
    }

    private fun initProps() {
        ContextCompat.getColor(context, R.color.black).let(::setCardBackgroundColor)
        radius = resources.getDimensionPixelOffset(R.dimen.record_type_card_corner_radius).toFloat()
        // TODO doesn't work here for some reason, need to set in the layout
        cardElevation = resources.getDimensionPixelOffset(R.dimen.record_type_card_elevation).toFloat()
        preventCornerOverlap = false
        useCompatPadding = true
    }

    private fun initAttrs(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
    ) {
        context.obtainStyledAttributes(attrs, R.styleable.RecordView, defStyleAttr, 0)
            .run {
                if (hasValue(R.styleable.RecordView_itemName)) {
                    itemName = getString(R.styleable.RecordView_itemName).orEmpty()
                }

                if (hasValue(R.styleable.RecordView_itemColor)) {
                    itemColor = getColor(R.styleable.RecordView_itemColor, Color.BLACK)
                }

                if (hasValue(R.styleable.RecordView_itemTagName)) {
                    itemTagName = getString(R.styleable.RecordView_itemTagName).orEmpty()
                }

                if (hasValue(R.styleable.RecordView_itemTagColor)) {
                    itemTagColor = getColor(R.styleable.RecordView_itemTagColor, Color.WHITE)
                }

                if (hasValue(R.styleable.RecordView_itemIcon)) {
                    itemIcon = getResourceId(R.styleable.RecordView_itemIcon, R.drawable.unknown)
                        .let(RecordTypeIcon::Image)
                }

                if (hasValue(R.styleable.RecordView_itemIconText)) {
                    itemIcon = getString(R.styleable.RecordView_itemIconText).orEmpty()
                        .let(RecordTypeIcon::Text)
                }

                if (hasValue(R.styleable.RecordView_itemTimeStarted)) {
                    itemTimeStarted = getString(R.styleable.RecordView_itemTimeStarted).orEmpty()
                }

                if (hasValue(R.styleable.RecordView_itemTimeEnded)) {
                    itemTimeEnded = getString(R.styleable.RecordView_itemTimeEnded).orEmpty()
                }

                if (hasValue(R.styleable.RecordView_itemDuration)) {
                    itemDuration = getString(R.styleable.RecordView_itemDuration).orEmpty()
                }

                if (hasValue(R.styleable.RecordView_itemComment)) {
                    itemComment = getString(R.styleable.RecordView_itemComment).orEmpty()
                }

                recycle()
            }
    }

    private fun setItemName() = with(binding) {
        if (itemTagName.isEmpty()) {
            tvRecordItemName.text = itemName
        } else {
            val name = "$itemName - $itemTagName"
            tvRecordItemName.text = name.toSpannableString().setForegroundSpan(
                color = itemTagColor,
                start = itemName.length,
                length = name.length - itemName.length,
            )
        }
    }

    private fun updateSeparatorVisibility() {
        binding.tvRecordItemTimeSeparator.visible =
            itemTimeStarted.isNotEmpty() && itemTimeEnded.isNotEmpty()
    }
}