package com.example.util.simpletimetracker.core.extension

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.example.util.simpletimetracker.core.utils.ITEM_ALPHA_DEFAULT
import com.example.util.simpletimetracker.core.utils.ITEM_ALPHA_SELECTED
import com.example.util.simpletimetracker.core.utils.ITEM_SCALE_DEFAULT
import com.example.util.simpletimetracker.core.utils.ITEM_SCALE_SELECTED
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.feature_base_adapter.BaseRecyclerAdapter
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_views.extension.dpToPx
import com.example.util.simpletimetracker.feature_views.extension.spToPx
import java.util.Collections

fun RecyclerView.changeDragSensitivity(scale: Float) = runCatching {
    val touchSlopField = RecyclerView::class.java.getDeclaredField("mTouchSlop")
    touchSlopField.isAccessible = true
    val touchSlop = touchSlopField.get(this) as Int
    touchSlopField.set(this, (touchSlop * scale).toInt())
}

fun RecyclerView.horizontalSmoothScrollWithOffset(
    snapPreference: Int,
    position: Int,
    calculateOffset: (recycler: View, view: View) -> Int,
) {
    val recycler = this
    val layoutManager = layoutManager as? LinearLayoutManager ?: return
    val smoothScroller = object : LinearSmoothScroller(context) {
        override fun getHorizontalSnapPreference(): Int = snapPreference

        override fun calculateDxToMakeVisible(view: View, snapPreference: Int): Int {
            val offset = calculateOffset(recycler, view)
            return super.calculateDxToMakeVisible(view, snapPreference) + offset
        }
    }

    smoothScroller.targetPosition = position
    layoutManager.startSmoothScroll(smoothScroller)
}

fun RecyclerView.onItemMoved(
    getIsSelectable: (RecyclerView.ViewHolder?) -> Boolean = { true },
    getSelectablePositions: ((RecyclerView.ViewHolder?) -> Pair<Int, Int>)? = null,
    onSelected: (RecyclerView.ViewHolder?) -> Unit = {},
    onClear: (RecyclerView.ViewHolder) -> Unit = {},
    onMoved: (items: List<ViewHolderType>, from: Int, to: Int) -> Unit = { _, _, _ -> },
) {
    val dragDirections = ItemTouchHelper.DOWN or ItemTouchHelper.UP or
        ItemTouchHelper.START or ItemTouchHelper.END

    fun getNewItems(
        adapter: BaseRecyclerAdapter,
        fromPosition: Int,
        toPosition: Int,
    ): List<ViewHolderType> {
        val newList = adapter.currentList.toList()

        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(newList, i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(newList, i, i - 1)
            }
        }

        return newList
    }

    val helper = object : ItemTouchHelper.SimpleCallback(0, 0) {

        override fun getDragDirs(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
        ): Int {
            return if (getIsSelectable(viewHolder)) dragDirections else 0
        }

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder,
        ): Boolean {
            val fromPosition = viewHolder.adapterPosition
            val toPosition = target.adapterPosition

            getSelectablePositions?.invoke(viewHolder)?.let { (start, end) ->
                if (toPosition < start) return false
                if (toPosition > end) return false
            }

            (adapter as? BaseRecyclerAdapter)?.let { adapter ->
                val newItems = getNewItems(adapter, fromPosition, toPosition)
                adapter.submitList(newItems)
                onMoved(newItems, fromPosition, toPosition)
            }

            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            // Do nothing
        }

        override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
            super.onSelectedChanged(viewHolder, actionState)
            if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) onSelected(viewHolder)
        }

        override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
            super.clearView(recyclerView, viewHolder)
            onClear(viewHolder)
        }
    }

    (ItemTouchHelper(helper)).attachToRecyclerView(this)
}

fun RecyclerView.onItemSwiped(
    @DrawableRes startIconRes: Int,
    @DrawableRes endIconRes: Int,
    @ColorInt iconColor: Int,
    startText: String,
    endText: String,
    @ColorInt textColor: Int,
    @ColorInt backgroundColor: Int,
    getIsSelectable: (RecyclerView.ViewHolder?) -> Boolean = { true },
    onSwipedStart: (RecyclerView.ViewHolder?) -> Unit,
    onSwipedEnd: (RecyclerView.ViewHolder?) -> Unit,
) {
    fun getIcon(@DrawableRes resId: Int): Drawable? {
        return ContextCompat.getDrawable(context, resId)?.mutate()?.apply { setTint(iconColor) }
    }

    fun getTextPaint(): Paint {
        return Paint().apply {
            isAntiAlias = true
            color = textColor
            textSize = 14.spToPx().toFloat()
            typeface = Typeface.DEFAULT_BOLD
        }
    }

    fun Paint.getTextHeight(text: String): Float {
        val bounds = Rect(0, 0, 0, 0)
        getTextBounds(text, 0, text.length, bounds)
        return bounds.height().toFloat()
    }

    val swipeDirections = ItemTouchHelper.START or ItemTouchHelper.END
    val startIcon by lazy { getIcon(startIconRes) }
    val endIcon by lazy { getIcon(endIconRes) }
    val iconMargin = 16.dpToPx()
    val background = ColorDrawable()
    val clearPaint = Paint().apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }
    val startTextHeight: Float
    val startTextPaint = getTextPaint().apply {
        textAlign = Paint.Align.LEFT
    }.also {
        startTextHeight = it.getTextHeight(startText)
    }
    val endTextHeight: Float
    val endTextPaint = getTextPaint().apply {
        textAlign = Paint.Align.RIGHT
    }.also {
        endTextHeight = it.getTextHeight(endText)
    }

    val helper = object : ItemTouchHelper.SimpleCallback(0, 0) {

        override fun getSwipeDirs(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
        ): Int {
            return if (getIsSelectable(viewHolder)) swipeDirections else 0
        }

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder,
        ): Boolean {
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            when (direction) {
                ItemTouchHelper.START -> {
                    onSwipedStart(viewHolder)
                }
                ItemTouchHelper.END -> {
                    // Prevent item removal.
                    ItemTouchHelper.Callback.getDefaultUIUtil().clearView(viewHolder.itemView)
                    adapter?.notifyItemChanged(viewHolder.adapterPosition)
                    onSwipedEnd(viewHolder)
                }
            }
        }

        override fun onChildDraw(
            canvas: Canvas,
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            dX: Float,
            dY: Float,
            actionState: Int,
            isCurrentlyActive: Boolean,
        ) {
            val itemView = viewHolder.itemView
            val isCanceled = dX == 0f

            if (isCanceled) {
                canvas.drawRect(
                    itemView.right + dX,
                    itemView.top.toFloat(),
                    itemView.right.toFloat(),
                    itemView.bottom.toFloat(),
                    clearPaint,
                )
                super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, false)
                return
            }

            // Draw the delete background
            background.color = backgroundColor
            background.setBounds(
                if (dX > 0) itemView.left else itemView.right + dX.toInt(),
                itemView.top,
                if (dX > 0) itemView.left + dX.toInt() else itemView.right,
                itemView.bottom,
            )
            background.draw(canvas)

            // Calculate position of the icon.
            val iconToDraw = if (dX > 0) startIcon else endIcon
            val itemHeight = itemView.bottom - itemView.top
            val iconTop = itemView.top + (itemHeight - iconToDraw?.intrinsicHeight.orZero()) / 2
            val iconBottom = iconTop + iconToDraw?.intrinsicHeight.orZero()
            val iconLeft: Int
            val iconRight: Int
            if (dX > 0) {
                iconLeft = itemView.left + iconMargin
                iconRight = itemView.left + iconMargin + iconToDraw?.intrinsicWidth.orZero()
            } else {
                iconLeft = itemView.right - iconMargin - iconToDraw?.intrinsicWidth.orZero()
                iconRight = itemView.right - iconMargin
            }

            // Draw the icon.
            iconToDraw?.setBounds(iconLeft, iconTop, iconRight, iconBottom)
            iconToDraw?.draw(canvas)

            // Draw the text.
            if (dX > 0) {
                canvas.drawText(
                    startText,
                    iconRight.toFloat() + iconMargin,
                    itemView.top.toFloat() + itemView.height / 2 + startTextHeight / 2,
                    startTextPaint,
                )
            } else {
                canvas.drawText(
                    endText,
                    iconLeft.toFloat() - iconMargin,
                    itemView.top.toFloat() + itemView.height / 2 + endTextHeight / 2,
                    endTextPaint,
                )
            }

            super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        }
    }

    (ItemTouchHelper(helper)).attachToRecyclerView(this)
}

fun RecyclerView.ViewHolder?.setItemSelected() = this?.run {
    itemView.alpha = ITEM_ALPHA_SELECTED
    itemView.scaleX = ITEM_SCALE_SELECTED
    itemView.scaleY = ITEM_SCALE_SELECTED
}

fun RecyclerView.ViewHolder.setItemUnselected() = run {
    itemView.alpha = ITEM_ALPHA_DEFAULT
    itemView.scaleX = ITEM_SCALE_DEFAULT
    itemView.scaleY = ITEM_SCALE_DEFAULT
}