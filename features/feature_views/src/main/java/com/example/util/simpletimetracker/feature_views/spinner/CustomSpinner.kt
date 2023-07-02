package com.example.util.simpletimetracker.feature_views.spinner

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import com.example.util.simpletimetracker.feature_views.R
import com.example.util.simpletimetracker.feature_views.databinding.SpinnerLayoutBinding
import com.example.util.simpletimetracker.feature_views.extension.onItemSelected
import com.example.util.simpletimetracker.feature_views.extension.setOnClick

class CustomSpinner @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(
    context,
    attrs,
    defStyleAttr
) {

    var onItemSelected: (CustomSpinnerItem) -> Unit = {}
    var onPositionSelected: (Int) -> Unit = {}

    private val binding: SpinnerLayoutBinding = SpinnerLayoutBinding
        .inflate(LayoutInflater.from(context), this, true)

    private var adapter: ArrayAdapter<String>? = null
    private var items: List<CustomSpinnerItem> = emptyList()
    private var isFromUser: Boolean = false

    init {
        val checkable: Boolean
        context.obtainStyledAttributes(attrs, R.styleable.CustomSpinner, defStyleAttr, 0)
            .run {
                checkable = getBoolean(R.styleable.CustomSpinner_customSpinnerCheckable, true)
                recycle()
            }
        val itemLayoutResId = if (checkable) {
            R.layout.item_spinner_checkable_layout
        } else {
            R.layout.item_spinner_layout
        }
        adapter = ArrayAdapter(context, itemLayoutResId)
        binding.customSpinner.adapter = adapter
        setOnClick {
            isFromUser = true
            binding.customSpinner.performClick()
        }
    }

    fun setData(items: List<CustomSpinnerItem>, selectedPosition: Int) {
        this.items = items
        adapter?.clear()
        adapter?.addAll(items.map(CustomSpinnerItem::text))

        // Reset listeners
        val itemSelectedListener = onItemSelected
        val positionSelectedListener = onPositionSelected
        binding.customSpinner.onItemSelectedListener = null
        onItemSelected = {}
        onPositionSelected = {}

        // Calling setSelection(int, boolean) because it sets selection internally and listener isn't called later.
        binding.customSpinner.setSelection(selectedPosition, false)

        binding.customSpinner.onItemSelected(
            onNothingSelected = {
                isFromUser = false
            },
            onPositionSelected = {
                if (isFromUser) {
                    items.getOrNull(it)?.let(onItemSelected::invoke)
                    onPositionSelected(it)
                    isFromUser = false
                }
            }
        )

        // Restore listeners
        onItemSelected = itemSelectedListener
        onPositionSelected = positionSelectedListener
    }

    fun setProcessSameItemSelection(enabled: Boolean) {
        binding.customSpinner.setProcessSameItemSelection(enabled)
    }

    abstract class CustomSpinnerItem {
        abstract val text: String
    }

    data class CustomSpinnerTextItem(
        override val text: String,
    ) : CustomSpinnerItem()
}
