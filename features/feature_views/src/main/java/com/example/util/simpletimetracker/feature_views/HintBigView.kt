package com.example.util.simpletimetracker.feature_views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.example.util.simpletimetracker.feature_views.databinding.HintBigViewLayoutBinding
import com.example.util.simpletimetracker.feature_views.extension.setOnClick

class HintBigView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : ConstraintLayout(
    context,
    attrs,
    defStyleAttr,
) {

    private val binding: HintBigViewLayoutBinding = HintBigViewLayoutBinding
        .inflate(LayoutInflater.from(context), this)

    var itemText: String = ""
        set(value) {
            field = value
            binding.tvHintBigText.text = value
        }

    var itemInfoIconVisible: Boolean = false
        set(value) {
            field = value
            binding.ivHintBigIcon.isVisible = value
        }

    var itemCloseIconVisible: Boolean = false
        set(value) {
            field = value
            binding.btnHintBigClose.isVisible = value
        }

    var itemActionButtonVisible: Boolean = false
        set(value) {
            field = value
            binding.btnHintBigAction.isVisible = value
        }

    var itemActionButtonText: String = ""
        set(value) {
            field = value
            binding.btnHintBigAction.text = value
        }

    init {
        initAttrs(context, attrs, defStyleAttr)
    }

    fun setOnCloseClick(action: () -> Unit) {
        binding.btnHintBigClose.setOnClick(action)
    }

    fun setOnActionClick(action: () -> Unit) {
        binding.btnHintBigAction.setOnClick(action)
    }

    private fun initAttrs(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
    ) {
        context.obtainStyledAttributes(attrs, R.styleable.HintBigView, defStyleAttr, 0)
            .run {
                if (hasValue(R.styleable.HintBigView_itemHintText)) {
                    itemText = getString(
                        R.styleable.HintBigView_itemHintText,
                    ).orEmpty()
                }

                if (hasValue(R.styleable.HintBigView_itemInfoIconVisible)) {
                    itemInfoIconVisible = getBoolean(
                        R.styleable.HintBigView_itemInfoIconVisible, false,
                    )
                }

                if (hasValue(R.styleable.HintBigView_itemCloseIconVisible)) {
                    itemCloseIconVisible = getBoolean(
                        R.styleable.HintBigView_itemCloseIconVisible, false,
                    )
                }

                if (hasValue(R.styleable.HintBigView_itemActionButtonVisible)) {
                    itemActionButtonVisible = getBoolean(
                        R.styleable.HintBigView_itemActionButtonVisible, false,
                    )
                }

                if (hasValue(R.styleable.HintBigView_itemActionButtonText)) {
                    itemActionButtonText = getString(
                        R.styleable.HintBigView_itemActionButtonText,
                    ).orEmpty()
                }

                recycle()
            }
    }
}