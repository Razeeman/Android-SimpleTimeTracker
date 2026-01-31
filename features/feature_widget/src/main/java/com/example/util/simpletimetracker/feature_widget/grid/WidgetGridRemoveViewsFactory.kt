package com.example.util.simpletimetracker.feature_widget.grid

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Size
import android.view.View
import android.view.ViewGroup
import android.widget.RemoteViews
import com.example.util.simpletimetracker.core.interactor.CompleteTypesStateInteractor
import com.example.util.simpletimetracker.core.interactor.FilterGoalsByDayOfWeekInteractor
import com.example.util.simpletimetracker.core.interactor.GetCurrentRecordsDurationInteractor
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.IconMapper
import com.example.util.simpletimetracker.core.mapper.RecordTypeViewDataMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.core.utils.PendingIntents
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.record.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.record.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.domain.record.model.Record
import com.example.util.simpletimetracker.domain.record.model.RunningRecord
import com.example.util.simpletimetracker.domain.recordType.interactor.RecordTypeGoalInteractor
import com.example.util.simpletimetracker.domain.recordType.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.recordType.model.RecordType
import com.example.util.simpletimetracker.domain.recordType.model.RecordTypeGoal
import com.example.util.simpletimetracker.feature_views.ColorUtils
import com.example.util.simpletimetracker.feature_views.GoalCheckmarkView
import com.example.util.simpletimetracker.feature_views.RecordTypeView
import com.example.util.simpletimetracker.feature_views.extension.dpToPx
import com.example.util.simpletimetracker.feature_views.extension.getBitmapFromView
import com.example.util.simpletimetracker.feature_views.extension.ifNull
import com.example.util.simpletimetracker.feature_views.extension.measureExactly
import com.example.util.simpletimetracker.feature_views.extension.setAllMargins
import com.example.util.simpletimetracker.feature_widget.R
import com.example.util.simpletimetracker.feature_widget.common.WidgetGetActualFilteredIdsInteractor
import com.example.util.simpletimetracker.feature_widget.common.WidgetViewsHolder
import com.example.util.simpletimetracker.feature_widget.grid.WidgetGridProvider.Companion.CONTROLS_NEW_PAGE
import com.example.util.simpletimetracker.feature_widget.grid.WidgetGridProvider.Companion.ITEM_CLICK_ACTION
import com.example.util.simpletimetracker.feature_widget.grid.WidgetGridProvider.Companion.NEW_PAGE_EXTRA
import com.example.util.simpletimetracker.feature_widget.grid.WidgetGridProvider.Companion.TYPE_ID_EXTRA
import com.example.util.simpletimetracker.feature_widget.utils.setRecordTypeTimers
import javax.inject.Inject
import kotlin.math.ceil

class WidgetGridRemoveViewsFactory @Inject constructor(
    private val resourceRepo: ResourceRepo,
    private val iconMapper: IconMapper,
    private val colorMapper: ColorMapper,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val runningRecordInteractor: RunningRecordInteractor,
    private val recordInteractor: RecordInteractor,
    private val prefsInteractor: PrefsInteractor,
    private val completeTypesStateInteractor: CompleteTypesStateInteractor,
    private val widgetViewsHolder: WidgetViewsHolder,
    private val recordTypeViewDataMapper: RecordTypeViewDataMapper,
    private val filterGoalsByDayOfWeekInteractor: FilterGoalsByDayOfWeekInteractor,
    private val recordTypeGoalInteractor: RecordTypeGoalInteractor,
    private val getCurrentRecordsDurationInteractor: GetCurrentRecordsDurationInteractor,
    private val widgetGetActualFilteredIdsInteractor: WidgetGetActualFilteredIdsInteractor,
) {

    private var preparedView: RecordTypeView? = null

    suspend fun getView(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
    ): RemoteViews {
        val allTypes = recordTypeInteractor.getAll().filter { !it.hidden }
        val widgetData = prefsInteractor.getGridWidgetData(appWidgetId)
        val filteredTypeIds = widgetGetActualFilteredIdsInteractor.execute(
            filterType = widgetData.filteringType,
            widgetItemIds = widgetData.typeIds,
            allItemIds = allTypes.map(RecordType::id).toSet(),
        )
        val recordTypes = allTypes.filter { it.id !in filteredTypeIds }
        val runningRecords = runningRecordInteractor.getAll()
        val isDarkTheme = prefsInteractor.getDarkMode()
        val backgroundTransparency = prefsInteractor.getWidgetBackgroundTransparencyPercent()
        val prevRecords = if (prefsInteractor.getRetroactiveTrackingMode()) {
            recordInteractor.getAllPrev(timeStarted = System.currentTimeMillis())
        } else {
            emptyList()
        }
        val goals = filterGoalsByDayOfWeekInteractor
            .execute(recordTypeGoalInteractor.getAllTypeGoals())
            .groupBy { it.idData.value }
        val allDailyCurrents = if (goals.isNotEmpty()) {
            getCurrentRecordsDurationInteractor.getAllDailyCurrents(
                typeIds = recordTypes.map(RecordType::id),
                runningRecords = runningRecords,
            )
        } else {
            // No goals - no need to calculate durations.
            emptyMap()
        }
        val options = appWidgetManager.getAppWidgetOptions(appWidgetId)
        val widgetSize = getWidgetSize(options)
        val gridSize = getGridSize(context, widgetSize)
        val pageSize = gridSize.columnCount * gridSize.rowCount
        val areControlsVisible = pageSize < recordTypes.size
        val lastPage = (recordTypes.size / pageSize)
            .let { if (recordTypes.size % pageSize == 0) it - 1 else it }
            .coerceAtLeast(0)
        val pageNumber = prefsInteractor.getGridWidget(appWidgetId)
        val pagesShift = pageNumber.coerceIn(0, lastPage) * pageSize
        val rows = recordTypes
            .drop(pagesShift)
            .take(pageSize)
            .chunked(gridSize.columnCount)

        val views = RemoteViews(context.packageName, R.layout.widgets_layout)
        views.removeAllViews(R.id.containerGridWidgets)

        val emptyHintVisibility = if (recordTypes.isEmpty()) View.VISIBLE else View.GONE
        views.setViewVisibility(R.id.containerGridWidgetsEmpty, emptyHintVisibility)

        val arrowVisibility = if (areControlsVisible) View.VISIBLE else View.GONE
        views.setViewVisibility(R.id.ivGridWidgetPrev, arrowVisibility)
        views.setViewVisibility(R.id.ivGridWidgetNext, arrowVisibility)
        views.setViewVisibility(R.id.btnGridWidgetPrev, arrowVisibility)
        views.setViewVisibility(R.id.btnGridWidgetNext, arrowVisibility)

        if (areControlsVisible) {
            val prevPage = (pageNumber - 1)
                .takeUnless { it < 0 }
                .ifNull { lastPage }
            val prevIntent = getPagePendingIntent(context, appWidgetId, prevPage, isNext = false)
            views.setOnClickPendingIntent(R.id.btnGridWidgetPrev, prevIntent)
            val nextPage = (pageNumber + 1)
                .takeUnless { it > lastPage }
                .orZero()
            val nextIntent = getPagePendingIntent(context, appWidgetId, nextPage, isNext = true)
            views.setOnClickPendingIntent(R.id.btnGridWidgetNext, nextIntent)

            views.setInt(R.id.ivGridWidgetPrev, "setColorFilter", Color.WHITE)
            views.setInt(R.id.ivGridWidgetNext, "setColorFilter", Color.WHITE)
        }

        rows.forEach { row ->
            val items = row.map { type ->
                getView(
                    context = context,
                    appWidgetId = appWidgetId,
                    widgetSize = widgetSize,
                    gridSize = gridSize,
                    recordType = type,
                    runningRecord = runningRecords.firstOrNull { record -> record.id == type.id },
                    prevRecord = prevRecords.firstOrNull { record -> record.typeId == type.id },
                    goals = goals,
                    allDailyCurrents = allDailyCurrents,
                    isDarkTheme = isDarkTheme,
                    backgroundTransparency = backgroundTransparency,
                )
            }
            val row = getRow(context, items, gridSize)

            views.addView(R.id.containerGridWidgets, row)
        }

        val emptyRows = (gridSize.rowCount - rows.size).takeIf { it > 0 }.orZero()
        repeat(emptyRows) {
            val emptyRow = getRow(context, emptyList(), gridSize)
            views.addView(R.id.containerGridWidgets, emptyRow)
        }

        return views
    }

    private fun getRow(
        context: Context,
        items: List<RemoteViews>,
        gridSize: GridSize,
    ): RemoteViews {
        val containerId = R.id.containerRowWidgets
        val views = RemoteViews(context.packageName, R.layout.widget_items_layout)

        fun addEmptyRowItem(isHalf: Boolean = false) {
            val layoutId = if (isHalf) R.layout.widget_half_item_layout else R.layout.widget_item_layout
            val item = RemoteViews(context.packageName, layoutId)
            views.addView(containerId, item)
        }

        val emptyItemsCount = gridSize.columnCount - items.size
        val sideEmptyItemsCount = emptyItemsCount / 2
        val needHalfItems = emptyItemsCount % 2 == 1
        views.removeAllViews(containerId)

        repeat(sideEmptyItemsCount) { addEmptyRowItem() }
        if (needHalfItems) addEmptyRowItem(isHalf = true)
        items.forEach { item -> views.addView(containerId, item) }
        if (needHalfItems) addEmptyRowItem(isHalf = true)
        repeat(sideEmptyItemsCount) { addEmptyRowItem() }

        return views
    }

    // TODO WIDGET:
    // TODO add settings activity to select number of cards
    // TODO add settings to filter some activities
    // TODO add settings for padding between cards?
    // TODO add repeat
    private fun getView(
        context: Context,
        appWidgetId: Int,
        widgetSize: Size?,
        gridSize: GridSize,
        recordType: RecordType,
        runningRecord: RunningRecord?,
        prevRecord: Record?,
        goals: Map<Long, List<RecordTypeGoal>>,
        allDailyCurrents: Map<Long, GetCurrentRecordsDurationInteractor.Result>,
        isDarkTheme: Boolean,
        backgroundTransparency: Long,
    ): RemoteViews {
        val isColored = when {
            runningRecord != null -> true
            prevRecord != null -> true
            else -> false
        }
        val view = prepareView(
            context = context,
            recordType = recordType,
            isColored = isColored,
            checkState = recordTypeViewDataMapper.mapGoalCheckmark(
                type = recordType,
                goals = goals,
                allDailyCurrents = allDailyCurrents,
            ),
            isComplete = recordType.id in completeTypesStateInteractor.widgetTypeIds,
            isDarkTheme = isDarkTheme,
            backgroundTransparency = backgroundTransparency,
        )
        measureView(
            context = context,
            widgetSize = widgetSize,
            gridSize = gridSize,
            view = view,
        )
        val bitmap = view.getBitmapFromView()

        val itemContainer = RemoteViews(context.packageName, R.layout.widget_item_layout)
        val item = RemoteViews(context.packageName, R.layout.widget_layout)
        setRecordTypeTimers(runningRecord, prevRecord, item)
        item.setImageViewBitmap(R.id.ivWidgetBackground, bitmap)
        val clickPendingIntent = getPendingIntent(context, appWidgetId, recordType.id)
        item.setOnClickPendingIntent(R.id.btnWidget, clickPendingIntent)
        itemContainer.addView(R.id.containerItemWidgets, item)

        return itemContainer
    }

    private fun prepareView(
        context: Context,
        recordType: RecordType,
        isColored: Boolean,
        checkState: GoalCheckmarkView.CheckState,
        isComplete: Boolean,
        isDarkTheme: Boolean,
        backgroundTransparency: Long,
    ): View {
        val icon = recordType.icon

        val name = recordType.name

        val textColor = if (isColored) {
            resourceRepo.getColor(R.color.colorIcon)
        } else {
            resourceRepo.getColor(R.color.widget_universal_empty_color)
        }

        val color = if (isColored) {
            colorMapper.mapToColorInt(recordType.color, isDarkTheme)
        } else {
            ColorUtils.changeAlpha(
                color = resourceRepo.getColor(R.color.widget_universal_background_color),
                alpha = 1f - backgroundTransparency / 100f,
            )
        }

        val view = getView(context).apply {
            (parent as? ViewGroup)?.removeAllViews()
            itemIcon = iconMapper.mapIcon(icon)
            itemName = name
            itemIconColor = textColor
            itemColor = color
            itemCheckState = checkState
            itemCompleteIsAnimated = false
            itemIsComplete = isComplete
        }

        return view
    }

    private fun measureView(
        context: Context,
        widgetSize: Size?,
        gridSize: GridSize,
        view: View,
    ) {
        val width = widgetSize?.width?.div(gridSize.columnCount) ?: getDefaultWidth(context)
        val height = widgetSize?.height?.div(gridSize.rowCount) ?: getDefaultHeight(context)
        view.measureExactly(width = width, height = height)
    }

    private fun getView(context: Context): RecordTypeView {
        preparedView?.let { return it }

        val view = widgetViewsHolder.getGridRecordTypeView(context).apply {
            getContainer().radius = resources
                .getDimensionPixelOffset(R.dimen.widget_universal_corner_radius).toFloat()
            getContainer().cardElevation = 0f
            getContainer().useCompatPadding = false
            getCheckmarkOutline().setAllMargins(4)
        }
        preparedView = view

        return view
    }

    private fun getWidgetSize(
        options: Bundle?,
    ): Size? {
        val width = options?.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 0)
            ?.dpToPx().takeUnless { it == 0 }
        val height = options?.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, 0)
            ?.dpToPx().takeUnless { it == 0 }

        return Size(
            width ?: return null,
            height ?: return null,
        )
    }

    private fun getGridSize(
        context: Context,
        widgetSize: Size?,
    ): GridSize {
        val cardWidth = getDefaultWidth(context)
        val cardHeight = getDefaultHeight(context)

        if (widgetSize == null) return GridSize(columnCount = 2, rowCount = 2)

        return GridSize(
            columnCount = ceil(widgetSize.width.toFloat() / cardWidth)
                .toInt().coerceAtLeast(1),
            rowCount = ceil(widgetSize.height.toFloat() / cardHeight)
                .toInt().coerceAtLeast(1),
        )
    }

    private fun getPendingIntent(
        context: Context,
        appWidgetId: Int,
        recordTypeId: Long,
    ): PendingIntent {
        val clickIntent = Intent(context, WidgetGridProvider::class.java)
        clickIntent.action = ITEM_CLICK_ACTION
        clickIntent.putExtra(TYPE_ID_EXTRA, recordTypeId)
        clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        return PendingIntent.getBroadcast(
            context,
            ClickRequestCode(
                appWidgetId = appWidgetId,
                recordTypeId = recordTypeId,
            ).hashCode(),
            clickIntent,
            PendingIntents.getFlags(),
        )
    }

    private fun getPagePendingIntent(
        context: Context,
        appWidgetId: Int,
        page: Int,
        isNext: Boolean,
    ): PendingIntent {
        val clickIntent = Intent(context, WidgetGridProvider::class.java)
        clickIntent.action = CONTROLS_NEW_PAGE
        clickIntent.putExtra(NEW_PAGE_EXTRA, page)
        clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        return PendingIntent.getBroadcast(
            context,
            PageRequestCode(appWidgetId, isNext).hashCode(),
            clickIntent,
            PendingIntents.getFlags(),
        )
    }

    private fun getDefaultWidth(context: Context): Int {
        return context.resources.getDimensionPixelSize(R.dimen.record_type_card_width)
    }

    private fun getDefaultHeight(context: Context): Int {
        return context.resources.getDimensionPixelSize(R.dimen.record_type_card_height)
    }

    data class GridSize(
        val columnCount: Int,
        val rowCount: Int,
    )

    private data class ClickRequestCode(
        val appWidgetId: Int,
        val recordTypeId: Long,
    )

    private data class PageRequestCode(
        val appWidgetId: Int,
        val isNext: Boolean,
    )
}