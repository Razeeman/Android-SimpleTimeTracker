package com.example.util.simpletimetracker.feature_widget.universal.customView

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import com.example.util.simpletimetracker.feature_widget.R
import kotlin.math.ceil
import kotlin.math.min
import kotlin.math.sqrt

class IconStackView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(
    context,
    attrs,
    defStyleAttr
) {

    // Attrs
    private var iconCountInEdit: Int = 0
    private var iconPadding: Int = 0
    private var iconBackgroundPadding: Int = 0
    // End of attrs

    private var data: List<Pair<Int, Int>> = emptyList()
    private val iconPaint: Paint = Paint()

    init {
        initArgs(context, attrs, defStyleAttr)
        initPaint()
        initEditMode()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val w = resolveSize(0, widthMeasureSpec)
        val h = resolveSize(0, heightMeasureSpec)

        setMeasuredDimension(w, h)
    }

    override fun onDraw(canvas: Canvas?) {
        if (canvas == null || data.isEmpty()) return

        val w = width.toFloat()
        val h = height.toFloat()

        when (data.size) {
            1 -> drawOne(data, canvas, w, h)
            2 -> drawTwo(data, canvas, w, h)
            else -> drawMany(data, canvas, w, h)
        }
    }

    fun setData(data: List<Pair<Int, Int>>) {
        this.data = data
        invalidate()
    }

    private fun drawOne(data: List<Pair<Int, Int>>, canvas: Canvas, w: Float, h: Float) {
        if (data.isEmpty()) return

        val radius = min(w / 2, h / 2)

        // Move to center
        canvas.translate(w / 2, h / 2)
        // Draw one
        drawIcon(data.first(), canvas, radius)
    }

    private fun drawTwo(data: List<Pair<Int, Int>>, canvas: Canvas, w: Float, h: Float) {
        if (data.size < 2) return

        val isVertical = h > w
        val radius = if (isVertical) {
            min(w, h / 2) / 2
        } else {
            min(w / 2, h) / 2
        }

        // Move to center
        canvas.translate(w / 2, h / 2)

        // Draw first
        if (isVertical) {
            canvas.translate(0f, -radius)
        } else {
            canvas.translate(-radius, 0f)
        }
        drawIcon(data[0], canvas, radius)

        // Draw second
        if (isVertical) {
            canvas.translate(0f, radius * 2)
        } else {
            canvas.translate(radius * 2, 0f)
        }
        drawIcon(data[1], canvas, radius)
    }

    private fun drawMany(data: List<Pair<Int, Int>>, canvas: Canvas, w: Float, h: Float) {
        if (data.isEmpty()) return

        val boxSize = min(w, h)
        val rowCount: Int = ceil(sqrt(data.size.toFloat())).toInt()
        val radius = boxSize / (2 * rowCount)

        // Move to center
        canvas.translate(w / 2, h / 2)
        // Move to top left corner of the box
        canvas.translate(-boxSize / 2, -boxSize / 2)
        // Move to first icon center
        canvas.translate(radius, radius)

        // Draw all
        data.forEachIndexed { index, dataPoint ->
            canvas.save()
            canvas.translate((index % rowCount) * 2 * radius, (index / rowCount) * 2 * radius)
            drawIcon(dataPoint, canvas, radius)
            canvas.restore()
        }
    }

    private fun drawIcon(data: Pair<Int, Int>, canvas: Canvas, radius: Float) {
        val backgroundRadius = radius - iconBackgroundPadding
        iconPaint.color = data.second

        // Draw background
        canvas.drawCircle(0f, 0f, backgroundRadius, iconPaint)

        // Draw icon
        getIconDrawable(data.first)?.apply {
            bounds = (sqrt(2f) * backgroundRadius / 2 - iconPadding)
                .toInt()
                .let { Rect(-it, -it, it, it) }
            draw(canvas)
        }
    }

    private fun getIconDrawable(iconId: Int): Drawable? {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            (AppCompatResources.getDrawable(context, iconId) as? BitmapDrawable)?.apply {
                colorFilter = PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
            }
        } else {
            VectorDrawableCompat.create(resources, iconId, context.theme)?.apply {
                setTintList(ColorStateList.valueOf(Color.WHITE))
            }
        }
    }

    private fun initArgs(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
    ) {
        context.obtainStyledAttributes(attrs, R.styleable.IconStackView, defStyleAttr, 0)
            .run {
                iconCountInEdit = getInt(R.styleable.IconStackView_iconCountInEdit, 0)
                iconPadding = getDimensionPixelOffset(R.styleable.IconStackView_iconPadding, 0)
                iconBackgroundPadding = getDimensionPixelOffset(R.styleable.IconStackView_iconBackgroundPadding, 0)
                recycle()
            }
    }

    private fun initPaint() {
        iconPaint.apply {
            isAntiAlias = true
            color = Color.BLACK
            style = Paint.Style.FILL
        }
    }

    private fun initEditMode() {
        if (isInEditMode) {
            val segments = iconCountInEdit.takeIf { it != 0 } ?: 5

            (0 until segments).map { R.drawable.ic_desktop_windows_24px to Color.RED }.let(::setData)
        }
    }
}