package com.example.util.simpletimetracker.feature_dialogs.duration.customView

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.util.simpletimetracker.core.extension.setOnClick
import com.example.util.simpletimetracker.feature_dialogs.R
import kotlinx.android.synthetic.main.number_keyboard_layout.view.*

class NumberKeyboardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(
    context,
    attrs,
    defStyleAttr
) {

    var listener: ((Int) -> Unit)? = null

    private val textViews by lazy {
        listOf(
            tvNumberKeyboard0,
            tvNumberKeyboard1,
            tvNumberKeyboard2,
            tvNumberKeyboard3,
            tvNumberKeyboard4,
            tvNumberKeyboard5,
            tvNumberKeyboard6,
            tvNumberKeyboard7,
            tvNumberKeyboard8,
            tvNumberKeyboard9
        )
    }

    init {
        View.inflate(context, R.layout.number_keyboard_layout, this)

        context.obtainStyledAttributes(attrs, R.styleable.NumberKeyboardView, defStyleAttr, 0)
            .run {
                getColor(R.styleable.NumberKeyboardView_numberKeyboardTextColor, Color.BLACK)
                    .let(::setTextColor)
                getDimensionPixelSize(R.styleable.NumberKeyboardView_numberKeyboardTextSize, 14)
                    .toFloat().let(::setTextSize)

                recycle()
            }

        textViews.forEachIndexed { index, view ->
            view.setOnClick { listener?.invoke(index) }
        }
    }

    private fun setTextColor(textColor: Int) {
        textViews.forEach { it.setTextColor(textColor) }
    }

    private fun setTextSize(textSize: Float) {
        textViews.forEach { it.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize) }
    }
}