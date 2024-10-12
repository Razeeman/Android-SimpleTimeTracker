package com.example.util.simpletimetracker.domain.interactor

import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.mapper.AppColorMapper
import com.example.util.simpletimetracker.domain.model.AppColor
import com.example.util.simpletimetracker.domain.model.CardOrder
import com.example.util.simpletimetracker.domain.model.CardTagOrder
import java.util.Locale
import javax.inject.Inject
import kotlin.math.roundToInt

class SortCardsInteractor @Inject constructor(
    private val appColorMapper: AppColorMapper,
) {

    suspend fun <T> sort(
        cardOrder: CardOrder,
        manualOrderProvider: suspend () -> Map<Long, Long>,
        data: List<DataHolder<T>>,
    ): List<DataHolder<T>> {
        return data
            .let(::sortByName)
            .let {
                when (cardOrder) {
                    CardOrder.COLOR -> sortByColor(it)
                    CardOrder.MANUAL -> sortByManualOrder(it, manualOrderProvider)
                    CardOrder.NAME -> it
                }
            }
    }

    suspend fun <T> sortTags(
        cardTagOrder: CardTagOrder,
        manualOrderProvider: suspend () -> Map<Long, Long>,
        activityOrderProvider: suspend () -> Map<Long, Long>,
        data: List<DataHolder<T>>,
    ): List<DataHolder<T>> {
        return data
            .let(::sortByName)
            .let {
                when (cardTagOrder) {
                    CardTagOrder.COLOR -> sortByColor(it)
                    CardTagOrder.MANUAL -> sortByManualOrder(it, manualOrderProvider)
                    CardTagOrder.NAME -> it
                    CardTagOrder.ACTIVITY -> sortByManualOrder(it, activityOrderProvider)
                }
            }
    }

    private fun <T> sortByName(
        data: List<DataHolder<T>>,
    ): List<DataHolder<T>> {
        return data.sortedBy { it.name.lowercase(Locale.getDefault()) }
    }

    fun <T> sortByColor(
        data: List<DataHolder<T>>,
    ): List<DataHolder<T>> {
        return data
            .map { type ->
                type to appColorMapper.mapToColorInt(color = type.color)
            }
            .map { (type, colorInt) ->
                val hsv = appColorMapper.mapToHsv(colorInt)
                type to hsv
            }
            .sortedWith(
                compareBy(
                    // Round to int to prevent wiggling around floating points.
                    { -(it.second[0].roundToInt()) }, // reversed hue
                    { (it.second[1] * 100).roundToInt() }, // saturation
                    { (it.second[2] * 100).roundToInt() }, // value
                ),
            )
            .map { (type, _) ->
                type
            }
    }

    private suspend fun <T> sortByManualOrder(
        data: List<DataHolder<T>>,
        manualOrderProvider: suspend () -> Map<Long, Long>,
    ): List<DataHolder<T>> {
        val order = manualOrderProvider.invoke()
        return data
            .filter { it.id in order.keys }
            .sortedBy { order[it.id].orZero() } +
            data.filter { it.id !in order.keys }
                .sortedBy { it.id }
    }

    data class DataHolder<T>(
        val id: Long,
        val name: String,
        val color: AppColor,
        val data: T,
    )
}