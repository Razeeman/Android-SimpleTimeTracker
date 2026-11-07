package com.example.util.simpletimetracker.feature_dialogs.dateTime

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.RippleDrawable
import android.icu.util.Calendar
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.AbsListView
import android.widget.BaseAdapter
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.graphics.ColorUtils
import androidx.core.view.isVisible
import com.example.util.simpletimetracker.core.mapper.TimeMapper
import com.example.util.simpletimetracker.core.model.CalendarDate
import com.example.util.simpletimetracker.domain.daysOfWeek.model.DayOfWeek
import com.example.util.simpletimetracker.feature_dialogs.R
import com.example.util.simpletimetracker.feature_dialogs.databinding.PersianDatePickerViewLayoutBinding
import com.example.util.simpletimetracker.feature_views.extension.dpToPx
import com.example.util.simpletimetracker.feature_views.extension.getThemedAttr
import com.example.util.simpletimetracker.feature_views.extension.layoutInflater

class PersianDatePickerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : LinearLayout(context, attrs) {

    var selectedDate: CalendarDate = CalendarDate(1, 1, 1)
        private set
    var onDateSet: ((CalendarDate) -> Unit) = {}

    private lateinit var timeMapper: TimeMapper
    private var visibleMonth: CalendarDate = selectedDate.copy(day = 1)
    private var minDate: CalendarDate = selectedDate
    private var maxDate: CalendarDate = selectedDate
    private var firstDayOfWeek: DayOfWeek = DayOfWeek.MONDAY
    private var showingYears = false
    private val cells = mutableListOf<Pair<CalendarDate, TextView>>()
    private val accentColor = context.getThemedAttr(R.attr.colorSecondary)
    private val textColor = context.getThemedAttr(R.attr.appTextPrimaryColor)
    private val targetSize = 48.dpToPx()
    private val dayHeight = 38.dpToPx()

    private val binding = PersianDatePickerViewLayoutBinding.inflate(layoutInflater, this)

    init {
        orientation = VERTICAL
        binding.year.apply {
            background = ripple()
            accessibilityDelegate = buttonAccessibilityDelegate
            setOnClickListener { showYearSelector() }
        }
        binding.date.apply {
            background = ripple()
            accessibilityDelegate = buttonAccessibilityDelegate
            setOnClickListener { showCalendar() }
        }
        binding.previous.apply {
            background = ripple()
            setOnClickListener { moveMonth(-1) }
        }
        binding.next.apply {
            background = ripple()
            setOnClickListener { moveMonth(1) }
        }
    }

    fun initialize(
        selectedDate: CalendarDate,
        minDate: CalendarDate,
        maxDate: CalendarDate,
        firstDayOfWeek: DayOfWeek,
        timeMapper: TimeMapper,
    ) {
        this.timeMapper = timeMapper
        this.minDate = minDate
        this.maxDate = maxDate
        this.firstDayOfWeek = firstDayOfWeek
        this.selectedDate = clampDate(selectedDate)
        this.visibleMonth = this.selectedDate.copy(day = 1)
        showCalendar()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // Match the native picker's preferred width while fitting all seven columns on small screens.
        val width = resolveSize(304.dpToPx(), widthMeasureSpec)
        super.onMeasure(
            MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
            heightMeasureSpec,
        )
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        if (event.actionMasked == MotionEvent.ACTION_DOWN) {
            parent?.requestDisallowInterceptTouchEvent(true)
        }
        return super.onInterceptTouchEvent(event)
    }

    override fun onRtlPropertiesChanged(layoutDirection: Int) {
        super.onRtlPropertiesChanged(layoutDirection)
        // LinearLayout mirrors the logical weekday order once; only the arrow icons need flipping.
        val scale = if (layoutDirection == LAYOUT_DIRECTION_RTL) -1f else 1f
        binding.previous.scaleX = scale
        binding.next.scaleX = scale
    }

    private fun moveMonth(amount: Int) {
        val timestamp = addMonths(timeMapper.toTimestamp(visibleMonth), amount)
        val target = timeMapper.toCalendarDate(timestamp)
        if (!pageIntersectsBounds(target)) return
        visibleMonth = target
        renderMonth()
    }

    private fun showYearSelector() {
        showingYears = true
        binding.content.isVisible = false
        binding.years.isVisible = true
        updateHeader()
        binding.years.adapter = createYearAdapter()
        binding.years.setOnItemClickListener { _, _, position, _ ->
            selectYear(minDate.year + position)
        }
        binding.years.post {
            if (showingYears) {
                binding.years.setSelectionFromTop(
                    selectedDate.year - minDate.year,
                    (binding.years.height - targetSize) / 2,
                )
            }
        }
    }

    private fun createYearAdapter(): BaseAdapter {
        return object : BaseAdapter() {
            override fun getCount(): Int = maxDate.year - minDate.year + 1

            override fun getItem(position: Int): Int = minDate.year + position

            override fun getItemId(position: Int): Long = getItem(position).toLong()

            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val value = getItem(position)
                val selected = value == selectedDate.year
                return (convertView as? TextView ?: createLabel()).apply {
                    text = timeMapper.formatYear(
                        timestamp = timeMapper.toTimestamp(CalendarDate(value, 1, 1)),
                    )
                    textSize = if (selected) 24f else 16f
                    setTextColor(if (selected) accentColor else textColor)
                    typeface = Typeface.create(if (selected) "sans-serif-medium" else "sans-serif", Typeface.NORMAL)
                    isSelected = selected
                    background = ripple()
                    layoutParams = AbsListView.LayoutParams(LayoutParams.MATCH_PARENT, targetSize)
                }
            }
        }
    }

    private fun selectYear(value: Int) {
        val first = selectedDate.copy(year = value, day = 1)
        val days = timeMapper.getActualMaximum(
            startDate = timeMapper.toTimestamp(first),
            field = Calendar.DAY_OF_MONTH,
            firstDayOfWeek = firstDayOfWeek,
        )
        selectedDate = clampDate(first.copy(day = minOf(selectedDate.day, days)))
        showCalendar()
        onDateSet.invoke(selectedDate)
        binding.year.requestFocus()
    }

    private fun showCalendar() {
        showingYears = false
        binding.content.isVisible = true
        binding.years.isVisible = false
        visibleMonth = selectedDate.copy(day = 1)
        renderMonth()
    }

    private fun updateHeader() {
        val timestamp = timeMapper.toTimestamp(selectedDate)
        binding.year.text = timeMapper.formatYear(timestamp)
        binding.year.contentDescription = context.getString(R.string.date_picker_select)
        binding.year.isActivated = showingYears
        binding.year.setTextColor(
            ColorUtils.setAlphaComponent(
                Color.WHITE,
                if (showingYears) HEADER_ALPHA_ACTIVE else HEADER_ALPHA_INACTIVE,
            ),
        )
        binding.date.text = timeMapper.formatDayDate(timestamp)
        binding.date.isActivated = !showingYears
        binding.date.setTextColor(
            ColorUtils.setAlphaComponent(
                Color.WHITE,
                if (showingYears) HEADER_ALPHA_INACTIVE else HEADER_ALPHA_ACTIVE,
            ),
        )
    }

    private fun renderMonth() {
        val start = timeMapper.toTimestamp(visibleMonth)
        val days = timeMapper.getActualMaximum(start, Calendar.DAY_OF_MONTH, firstDayOfWeek)
        val today = timeMapper.toCalendarDate(System.currentTimeMillis())
        binding.month.text = timeMapper.formatMonthYear(start)
        updateHeader()
        val page = timeMapper.toCalendarDate(addMonths(start, -1))

        binding.previous.isEnabled = pageIntersectsBounds(page)
        binding.next.isEnabled = pageIntersectsBounds(
            timeMapper.toCalendarDate(addMonths(start, 1)),
        )
        binding.previous.alpha = if (binding.previous.isEnabled) 1f else DISABLED_ALPHA
        binding.next.alpha = if (binding.next.isEnabled) 1f else DISABLED_ALPHA

        binding.grid.removeAllViews()
        cells.clear()
        val weekdays = timeMapper.getWeekOrder(firstDayOfWeek)
        val headings = LinearLayout(context)
        weekdays.forEach { weekday ->
            headings.addView(
                createLabel().apply {
                    text = timeMapper.toNarrowDayOfWeekName(weekday)
                    contentDescription = timeMapper.toShortDayOfWeekName(weekday)
                    textSize = 12f
                    setTextColor(context.getThemedAttr(R.attr.appTextHintColor))
                },
                LayoutParams(0, 28.dpToPx(), 1f),
            )
        }
        binding.grid.addView(headings)

        val weekdayOfFirstDay = timeMapper.newCalendar().apply { timeInMillis = start }.get(Calendar.DAY_OF_WEEK) - 1
        val configuredFirstWeekday = timeMapper.toCalendarDayOfWeek(firstDayOfWeek) - 1
        val leading = (weekdayOfFirstDay - configuredFirstWeekday + 7) % 7
        val cellCount = 6 * 7
        var row = LinearLayout(context)
        repeat(cellCount) { index ->
            if (index % 7 == 0) {
                row = LinearLayout(context)
                binding.grid.addView(row)
            }
            val day = index - leading + 1
            val cell = if (day in 1..days) {
                createDay(visibleMonth.copy(day = day), today)
            } else {
                // Empty cells preserve the grid layout and should be skipped by accessibility services.
                View(context).apply { importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO }
            }
            row.addView(cell, LayoutParams(0, dayHeight, 1f))
        }
    }

    private fun createDay(date: CalendarDate, today: CalendarDate): TextView {
        val timestamp = timeMapper.toTimestamp(date)
        return createLabel().apply {
            val data = timeMapper.formatDayOfMonth(timestamp, false)
            text = data
            isEnabled = compareDates(date, minDate) >= 0 && compareDates(date, maxDate) <= 0
            isFocusable = true
            alpha = if (isEnabled) 1f else DISABLED_ALPHA
            accessibilityDelegate = buttonAccessibilityDelegate
            updateIndicator(this, date, today)
            setOnClickListener {
                selectedDate = date
                updateHeader()
                cells.forEach { (cellDate, view) -> updateIndicator(view, cellDate, today) }
                val hint = context.getString(R.string.date_picker_selected) + " " + data
                @Suppress("DEPRECATION")
                announceForAccessibility(hint)
                onDateSet.invoke(date)
            }
            cells += date to this
        }
    }

    private fun updateIndicator(view: TextView, date: CalendarDate, today: CalendarDate) {
        view.isSelected = date == selectedDate
        val textColor = when {
            view.isSelected -> Color.WHITE
            date == today -> accentColor
            else -> textColor
        }
        view.setTextColor(textColor)
        val circle = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(if (view.isSelected) accentColor else Color.TRANSPARENT)
        }
        view.background = RippleDrawable(
            ColorStateList.valueOf(ColorUtils.setAlphaComponent(accentColor, 60)),
            LayerDrawable(arrayOf(circle)).apply {
                setLayerSize(0, 36.dpToPx(), 36.dpToPx())
                setLayerGravity(0, Gravity.CENTER)
            },
            GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.WHITE)
            },
        )
    }

    private fun pageIntersectsBounds(page: CalendarDate): Boolean {
        // A month is available if it contains at least one date within the inclusive min/max bounds.
        val first = page.copy(day = 1)
        val day = timeMapper.getActualMaximum(
            startDate = timeMapper.toTimestamp(first),
            field = Calendar.DAY_OF_MONTH,
            firstDayOfWeek = firstDayOfWeek,
        )
        val last = first.copy(day = day)
        return compareDates(first, maxDate) <= 0 && compareDates(last, minDate) >= 0
    }

    private fun clampDate(date: CalendarDate): CalendarDate {
        return when {
            compareDates(date, minDate) < 0 -> minDate
            compareDates(date, maxDate) > 0 -> maxDate
            else -> date
        }
    }

    private fun createLabel(): TextView {
        return TextView(context).apply {
            gravity = Gravity.CENTER
            textSize = 12f
            setTextColor(textColor)
            setPadding(0, 0, 0, 0)
        }
    }

    private fun ripple(): RippleDrawable {
        return RippleDrawable(
            ColorStateList.valueOf(ColorUtils.setAlphaComponent(accentColor, 60)),
            null,
            GradientDrawable().apply { setColor(Color.WHITE) },
        )
    }

    private fun addMonths(
        timestamp: Long,
        amount: Int,
    ): Long {
        return timeMapper.newCalendar().apply {
            timeInMillis = timestamp
            add(Calendar.MONTH, amount)
        }.timeInMillis
    }

    private fun compareDates(first: CalendarDate, second: CalendarDate): Int {
        return compareValuesBy(
            first,
            second,
            CalendarDate::year,
            CalendarDate::month,
            CalendarDate::day,
        )
    }

    companion object {
        private const val DISABLED_ALPHA = 0.38f
        private const val HEADER_ALPHA_ACTIVE = 255
        private const val HEADER_ALPHA_INACTIVE = 179

        private val buttonAccessibilityDelegate = object : AccessibilityDelegate() {
            override fun onInitializeAccessibilityNodeInfo(host: View, info: AccessibilityNodeInfo) {
                super.onInitializeAccessibilityNodeInfo(host, info)
                // Expose clickable text views as buttons to accessibility services such as TalkBack.
                info.className = android.widget.Button::class.java.name
            }
        }
    }
}
