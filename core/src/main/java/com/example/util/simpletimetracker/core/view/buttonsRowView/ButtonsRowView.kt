package com.example.util.simpletimetracker.core.view.buttonsRowView

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.core.databinding.ButtonsRowViewLayoutBinding
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent

class ButtonsRowView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(
    context,
    attrs,
    defStyleAttr
) {

    var listener: ((ButtonsRowViewData) -> Unit)? = null

    val adapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createButtonsRowViewAdapterDelegate(selectedColor, ::onItemClick)
        )
    }

    private val binding: ButtonsRowViewLayoutBinding = ButtonsRowViewLayoutBinding
        .inflate(LayoutInflater.from(context), this)

    private val selectedColor by lazy {
        var defaultColor = ContextCompat.getColor(context, R.color.colorPrimary)
        runCatching {
            context.theme?.obtainStyledAttributes(intArrayOf(R.attr.appActiveColor))?.run {
                defaultColor = getColor(0, defaultColor)
                recycle()
            }
        }
        defaultColor
    }

    init {
        initRecycler()
        initEditMode()
    }

    fun onItemClick(buttonsRowViewData: ButtonsRowViewData) {
        listener?.invoke(buttonsRowViewData)
    }

    private fun initRecycler() {
        binding.rvButtonsRowView.apply {
            layoutManager = FlexboxLayoutManager(context).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.CENTER
                flexWrap = FlexWrap.NOWRAP
            }
            adapter = this@ButtonsRowView.adapter
        }
    }

    private fun initEditMode() {
        if (isInEditMode) {
            listOf(
                ButtonsRowTestViewData(1, "Button 1", true),
                ButtonsRowTestViewData(2, "Button 2", false),
                ButtonsRowTestViewData(3, "Button 3", false)
            ).let(adapter::replace)
        }
    }

    inner class ButtonsRowTestViewData(
        override val id: Long,
        override val name: String,
        override val isSelected: Boolean
    ) : ButtonsRowViewData()
}