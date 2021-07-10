package com.example.util.simpletimetracker.core.view.spinner

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.databinding.SpinnerLayoutBinding
import com.example.util.simpletimetracker.core.extension.onItemSelected
import com.example.util.simpletimetracker.core.extension.setOnClick

class CustomSpinner @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(
    context,
    attrs,
    defStyleAttr
) {

    var onItemSelected: (CustomSpinnerItem) -> Unit = {}
    var onPositionSelected: (Int) -> Unit = {}

    private val binding: SpinnerLayoutBinding = SpinnerLayoutBinding
        .inflate(LayoutInflater.from(context), this, true)

    private val adapter: ArrayAdapter<String> = ArrayAdapter(context, R.layout.item_spinner_layout)
    private var selectedPosition: Int = 0
    private var items: List<CustomSpinnerItem> = emptyList()

    init {
        binding.customSpinner.adapter = adapter
        binding.customSpinner.onItemSelected {
            if (selectedPosition != it) {
                selectedPosition = it
                items.getOrNull(it)?.let(onItemSelected::invoke)
                onPositionSelected(it)
            }
        }
        setOnClick { binding.customSpinner.performClick() }
    }

    fun setData(items: List<CustomSpinnerItem>, selectedPosition: Int) {
        this.selectedPosition = selectedPosition
        this.items = items
        adapter.clear()
        adapter.addAll(items.map(CustomSpinnerItem::text))
        binding.customSpinner.setSelection(selectedPosition)
    }

    abstract class CustomSpinnerItem {
        abstract val text: String
    }

    data class CustomSpinnerTextItem(
        override val text: String
    ) : CustomSpinnerItem()
}
