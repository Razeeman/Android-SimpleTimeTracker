package com.example.util.simpletimetracker.feature_settings.customView

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import com.example.util.simpletimetracker.core.extension.onItemSelected
import com.example.util.simpletimetracker.core.extension.setOnClick
import com.example.util.simpletimetracker.feature_settings.R
import kotlinx.android.synthetic.main.spinner_layout.view.*

class CustomSpinner @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(
    context,
    attrs,
    defStyleAttr
) {

    var onItemSelected: (Int) -> Unit = {}
    private val adapter: ArrayAdapter<String> = ArrayAdapter(context, R.layout.item_spinner_layout)
    private var selectedPosition: Int = 0

    init {
        View.inflate(context, R.layout.spinner_layout, this)

        customSpinner.adapter = adapter
        customSpinner.onItemSelected {
            tvCustomSpinner.text = adapter.getItem(it)
            if (selectedPosition != it) {
                selectedPosition = it
                onItemSelected(it)
            }
        }
        layoutCustomSpinner.setOnClick { customSpinner.performClick() }
    }

    fun setData(items: List<String>, selectedPosition: Int) {
        this.selectedPosition = selectedPosition
        adapter.clear()
        adapter.addAll(items)
        customSpinner.setSelection(selectedPosition)
        tvCustomSpinner.text = adapter.getItem(selectedPosition)
    }
}
