package com.example.util.simpletimetracker.feature_views

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import com.example.util.simpletimetracker.feature_views.databinding.RecordSimpleViewLayoutBinding
import com.example.util.simpletimetracker.feature_views.extension.getThemedAttr
import com.example.util.simpletimetracker.feature_views.viewData.RecordTypeIcon

class RecordSimpleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : ConstraintLayout(
    context,
    attrs,
    defStyleAttr,
) {

    private val binding: RecordSimpleViewLayoutBinding = RecordSimpleViewLayoutBinding
        .inflate(LayoutInflater.from(context), this)

    var itemName: String = ""
        set(value) {
            binding.tvRecordSimpleItemName.text = value
            field = value
        }

    var itemColor: Int = Color.BLACK
        set(value) {
            ViewCompat.setBackgroundTintList(
                binding.ivRecordSimpleItemBackground,
                ColorStateList.valueOf(value),
            )
            field = value
            updateTimeAccents()
        }

    var itemIcon: RecordTypeIcon = RecordTypeIcon.Image(0)
        set(value) {
            binding.ivRecordSimpleItemIcon.itemIcon = value
            field = value
        }

    var itemTimeStarted: String = ""
        set(value) {
            binding.tvRecordSimpleItemTimeStarted.text = value
            field = value
        }

    var itemTimeEnded: String = ""
        set(value) {
            if (value.isEmpty()) {
                binding.tvRecordSimpleItemTimeEnded.isVisible = false
                binding.tvRecordSimpleItemTimeDivider.isVisible = false
            } else {
                binding.tvRecordSimpleItemTimeEnded.isVisible = true
                binding.tvRecordSimpleItemTimeDivider.isVisible = true
                binding.tvRecordSimpleItemTimeEnded.text = value
            }
            field = value
        }

    var itemTimeStartedAccented: Boolean = false
        set(value) {
            field = value
            updateTimeAccents()
        }

    var itemTimeEndedAccented: Boolean = false
        set(value) {
            field = value
            updateTimeAccents()
        }

    var itemDuration: String = ""
        set(value) {
            binding.tvRecordSimpleItemDuration.text = value
            field = value
        }

    init {
        initAttrs(context, attrs, defStyleAttr)
    }

    private fun initAttrs(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
    ) {
        context.obtainStyledAttributes(attrs, R.styleable.RecordSimpleView, defStyleAttr, 0)
            .run {
                if (hasValue(R.styleable.RecordSimpleView_itemName)) {
                    itemName = getString(R.styleable.RecordSimpleView_itemName).orEmpty()
                }

                if (hasValue(R.styleable.RecordSimpleView_itemColor)) {
                    itemColor = getColor(R.styleable.RecordSimpleView_itemColor, Color.BLACK)
                }

                if (hasValue(R.styleable.RecordSimpleView_itemIcon)) {
                    itemIcon = getResourceId(R.styleable.RecordSimpleView_itemIcon, R.drawable.unknown)
                        .let(RecordTypeIcon::Image)
                }

                if (hasValue(R.styleable.RecordSimpleView_itemIconText)) {
                    itemIcon = getString(R.styleable.RecordSimpleView_itemIconText).orEmpty()
                        .let(RecordTypeIcon::Text)
                }

                if (hasValue(R.styleable.RecordSimpleView_itemTimeStarted)) {
                    itemTimeStarted = getString(R.styleable.RecordSimpleView_itemTimeStarted).orEmpty()
                }

                if (hasValue(R.styleable.RecordSimpleView_itemTimeEnded)) {
                    itemTimeEnded = getString(R.styleable.RecordSimpleView_itemTimeEnded).orEmpty()
                }

                if (hasValue(R.styleable.RecordSimpleView_itemTimeStartedAccented)) {
                    itemTimeStartedAccented =
                        getBoolean(R.styleable.RecordSimpleView_itemTimeStartedAccented, false)
                }

                if (hasValue(R.styleable.RecordSimpleView_itemTimeEndedAccented)) {
                    itemTimeEndedAccented =
                        getBoolean(R.styleable.RecordSimpleView_itemTimeEndedAccented, false)
                }

                if (hasValue(R.styleable.RecordSimpleView_itemDuration)) {
                    itemDuration = getString(R.styleable.RecordSimpleView_itemDuration).orEmpty()
                }

                recycle()
            }
    }

    private fun updateTimeAccents() {
        val accentColor = context.getThemedAttr(R.attr.colorAccent)
        val timeStartedBackgroundColor = if (itemTimeStartedAccented) {
            accentColor
        } else {
            itemColor
        }
        val timeEndedBackgroundColor = if (itemTimeEndedAccented) {
            accentColor
        } else {
            itemColor
        }

        ViewCompat.setBackgroundTintList(
            binding.tvRecordSimpleItemTimeStarted,
            ColorStateList.valueOf(timeStartedBackgroundColor),
        )
        ViewCompat.setBackgroundTintList(
            binding.tvRecordSimpleItemTimeEnded,
            ColorStateList.valueOf(timeEndedBackgroundColor),
        )
    }
}