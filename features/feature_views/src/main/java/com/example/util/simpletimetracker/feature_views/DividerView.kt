package com.example.util.simpletimetracker.feature_views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.util.simpletimetracker.feature_views.databinding.DividerViewBinding

class DividerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : ConstraintLayout(
    context,
    attrs,
    defStyleAttr
) {

    init {
        DividerViewBinding.inflate(LayoutInflater.from(context), this)
    }
}