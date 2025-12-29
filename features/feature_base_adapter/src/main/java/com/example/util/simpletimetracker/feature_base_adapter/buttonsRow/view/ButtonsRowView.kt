package com.example.util.simpletimetracker.feature_base_adapter.buttonsRow.view

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.example.util.simpletimetracker.feature_base_adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.databinding.ButtonsRowViewLayoutBinding
import com.example.util.simpletimetracker.feature_views.extension.layoutInflater
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent

class ButtonsRowView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(
    context,
    attrs,
    defStyleAttr,
) {

    var listener: ((ButtonsRowViewData) -> Unit)? = null

    private val adapter: BaseRecyclerAdapter by lazy {
        BaseRecyclerAdapter(
            createButtonsRowViewInternalAdapterDelegate(::onItemClick),
        )
    }

    private val binding = ButtonsRowViewLayoutBinding.inflate(layoutInflater, this)

    private var selectedPositionAnimator: ValueAnimator? = null
    private var updateSelectedPositionOnLayout: Boolean = false
    private val selectedPositionAnimationDuration: Float by lazy {
        resources.getInteger(android.R.integer.config_shortAnimTime) * 0.85f
    }

    init {
        initRecycler()
        initEditMode()
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        // In case container is not visible on adapter items replace.
        if (updateSelectedPositionOnLayout) {
            updateSelectedPositionOnLayout = false
            updateSelectedItem(adapter.currentList, isFast = false)
        }
    }

    fun replace(items: List<ViewHolderType>, isFast: Boolean = false) {
        if (isFast) {
            adapter.replaceFast(items)
        } else {
            adapter.replace(items)
        }
        binding.rvButtonsRowView.post {
            updateSelectedItem(items, isFast)
        }
    }

    private fun onItemClick(buttonsRowViewData: ButtonsRowViewData) {
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

    private fun updateSelectedItem(
        currentList: List<ViewHolderType>,
        isFast: Boolean,
    ) = with(binding) {
        val containerWidth = rvButtonsRowView.width
        if (containerWidth == 0) {
            updateSelectedPositionOnLayout = true
            return@with
        }
        val selectedItemIndex = currentList
            .indexOfFirst { it is ButtonsRowViewData && it.isSelected }
            .takeUnless { it == -1 }
            ?: run {
                btnButtonsRowSelected.isInvisible = true
                return@with
            }
        btnButtonsRowSelected.isVisible = true
        val itemsCount = currentList.size.takeIf { it > 0 }
            ?: return@with
        val itemWidth = containerWidth / itemsCount

        if (btnButtonsRowSelected.width != itemWidth) {
            btnButtonsRowSelected.updateLayoutParams { width = itemWidth }
        }

        val currentPosition = btnButtonsRowSelected.translationX
        val newPosition = itemWidth.toFloat() * selectedItemIndex
        if (currentPosition != newPosition) {
            if (isFast) {
                btnButtonsRowSelected.translationX = newPosition
            } else {
                selectedPositionAnimator?.cancel()
                val animator = ValueAnimator.ofFloat(currentPosition, newPosition)
                selectedPositionAnimator = animator
                animator.duration = selectedPositionAnimationDuration.toLong()
                animator.addUpdateListener { value ->
                    btnButtonsRowSelected.translationX = value.animatedValue as Float
                }
                animator.start()
            }
        }
    }

    private fun initEditMode() {
        if (isInEditMode) {
            listOf(
                ButtonsRowTestViewData(1, "Button 1", true),
                ButtonsRowTestViewData(2, "Button 2", false),
                ButtonsRowTestViewData(3, "Button 3", false),
            ).let(adapter::replace)
        }
    }

    inner class ButtonsRowTestViewData(
        override val id: Long,
        override val name: String,
        override val isSelected: Boolean,
    ) : ButtonsRowViewData()
}