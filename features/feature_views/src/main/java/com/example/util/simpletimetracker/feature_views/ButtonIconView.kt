package com.example.util.simpletimetracker.feature_views

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.core.content.withStyledAttributes
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.example.util.simpletimetracker.feature_views.databinding.ButtonIconViewLayoutBinding
import com.example.util.simpletimetracker.feature_views.extension.dpToPx
import com.example.util.simpletimetracker.feature_views.extension.layoutInflater
import com.example.util.simpletimetracker.feature_views.extension.setTextOptional

class ButtonIconView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(
    context,
    attrs,
    defStyleAttr,
) {

    private val binding = ButtonIconViewLayoutBinding.inflate(layoutInflater, this)

    var buttonIconSize: Size = Size.NORMAL
        set(value) {
            field = value
            setButtonHeight(value)
        }

    var buttonIconColor: Int = Color.BLACK
        set(value) {
            field = value
            binding.cardButtonIconContainer.setCardBackgroundColor(value)
        }

    var buttonIconText: String = ""
        set(value) {
            field = value
            binding.tvButtonIcon.setTextOptional(value)
        }

    var buttonIconVisible: Boolean = true
        set(value) {
            field = value
            binding.cardButtonIcon.isVisible = value
        }

    var buttonIconRes: Int = R.drawable.unknown
        set(value) {
            field = value
            binding.ivButtonIcon.setImageResource(value)
        }

    var buttonIconBackgroundColor: Int = Color.TRANSPARENT
        set(value) {
            field = value
            binding.cardButtonIcon.setCardBackgroundColor(value)
        }

    var buttonIconAlignedStart: Boolean = false
        set(value) {
            field = value
            setIconAlignedStart(value)
        }

    init {
        initAttrs(context, attrs, defStyleAttr)
    }

    private fun initAttrs(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
    ) {
        context.withStyledAttributes(attrs, R.styleable.ButtonIconView, defStyleAttr, 0) {
            if (hasValue(R.styleable.ButtonIconView_itemButtonIconSize)) {
                buttonIconSize = getInt(
                    R.styleable.ButtonIconView_itemButtonIconSize,
                    Size.NORMAL.value,
                ).let(Size.Companion::fromValue)
            }

            if (hasValue(R.styleable.ButtonIconView_itemButtonIconColor)) {
                buttonIconColor = getColor(
                    R.styleable.ButtonIconView_itemButtonIconColor, Color.BLACK,
                )
            }

            if (hasValue(R.styleable.ButtonIconView_itemButtonIconText)) {
                buttonIconText = getString(
                    R.styleable.ButtonIconView_itemButtonIconText,
                ).orEmpty()
            }

            if (hasValue(R.styleable.ButtonIconView_itemButtonIconVisible)) {
                buttonIconVisible = getBoolean(
                    R.styleable.ButtonIconView_itemButtonIconVisible, true,
                )
            }

            if (hasValue(R.styleable.ButtonIconView_itemButtonIconRes)) {
                buttonIconRes = getResourceId(
                    R.styleable.ButtonIconView_itemButtonIconRes, R.drawable.unknown,
                )
            }

            if (hasValue(R.styleable.ButtonIconView_itemButtonIconBackgroundColor)) {
                buttonIconBackgroundColor = getColor(
                    R.styleable.ButtonIconView_itemButtonIconBackgroundColor, Color.TRANSPARENT,
                )
            }

            if (hasValue(R.styleable.ButtonIconView_itemButtonIconAlignedStart)) {
                buttonIconAlignedStart = getBoolean(
                    R.styleable.ButtonIconView_itemButtonIconAlignedStart, false,
                )
            }
        }
    }

    private fun setButtonHeight(value: Size) = with(binding) {
        cardButtonIconContainer.updateLayoutParams {
            height = when (value) {
                Size.NORMAL -> resources.getDimensionPixelSize(R.dimen.input_field_height)
                Size.BIG -> resources.getDimensionPixelSize(R.dimen.input_field_height_big)
                Size.EXTRA_BIG -> resources.getDimensionPixelSize(R.dimen.input_field_height_extra_big)
            }
        }
        listOf(spaceButtonIconStart, spaceButtonIconEnd).forEach {
            val sizeDp = if (value == Size.NORMAL) 8 else 12
            it.updateLayoutParams { width = sizeDp.dpToPx() }
        }
    }

    private fun setIconAlignedStart(value: Boolean) = with(binding) {
        listOf(spaceButtonIcon1, spaceButtonIcon2).forEach {
            it.isVisible = value
        }
    }

    enum class Size(val value: Int) {
        NORMAL(value = 0),
        BIG(value = 1),
        EXTRA_BIG(value = 2),
        ;

        companion object {
            fun fromValue(value: Int): Size {
                return Size.entries.firstOrNull { it.value == value } ?: NORMAL
            }
        }
    }
}