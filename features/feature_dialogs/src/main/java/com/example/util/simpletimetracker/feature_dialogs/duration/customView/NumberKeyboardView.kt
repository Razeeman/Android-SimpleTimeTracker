package com.example.util.simpletimetracker.feature_dialogs.duration.customView

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.util.simpletimetracker.feature_views.extension.setOnClick
import com.example.util.simpletimetracker.feature_dialogs.R
import com.example.util.simpletimetracker.feature_dialogs.databinding.NumberKeyboardLayoutBinding

class NumberKeyboardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : ConstraintLayout(
    context,
    attrs,
    defStyleAttr,
) {

    var listener: ((Button) -> Unit)? = null

    private val binding: NumberKeyboardLayoutBinding = NumberKeyboardLayoutBinding
        .inflate(LayoutInflater.from(context), this)

    private val textViews by lazy {
        with(binding) {
            mapOf(
                0 to tvNumberKeyboard0,
                1 to tvNumberKeyboard1,
                2 to tvNumberKeyboard2,
                3 to tvNumberKeyboard3,
                4 to tvNumberKeyboard4,
                5 to tvNumberKeyboard5,
                6 to tvNumberKeyboard6,
                7 to tvNumberKeyboard7,
                8 to tvNumberKeyboard8,
                9 to tvNumberKeyboard9,
            )
        }
    }

    init {
        context.obtainStyledAttributes(attrs, R.styleable.NumberKeyboardView, defStyleAttr, 0)
            .run {
                getColor(R.styleable.NumberKeyboardView_numberKeyboardTextColor, Color.BLACK)
                    .let(::setTextColor)
                getDimensionPixelSize(R.styleable.NumberKeyboardView_numberKeyboardTextSize, 14)
                    .toFloat().let(::setTextSize)

                recycle()
            }

        textViews.forEach { (value, view) ->
            view.setOnClick { listener?.invoke(Button.Number(value)) }
        }
        binding.tvNumberKeyboard00.setOnClick { listener?.invoke(Button.DoubleZero) }
        binding.btnNumberKeyboardDelete.setOnClick { listener?.invoke(Button.Delete) }
    }

    private fun setTextColor(textColor: Int) {
        textViews.values.forEach { it.setTextColor(textColor) }
        binding.tvNumberKeyboard00.setTextColor(textColor)
        binding.ivNumberKeyboardDelete.imageTintList = ColorStateList.valueOf(textColor)
    }

    private fun setTextSize(textSize: Float) {
        textViews.values.forEach { it.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize) }
        binding.tvNumberKeyboard00.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
    }

    sealed interface Button {
        data class Number(val value: Int) : Button
        object DoubleZero : Button
        object Delete : Button
    }
}