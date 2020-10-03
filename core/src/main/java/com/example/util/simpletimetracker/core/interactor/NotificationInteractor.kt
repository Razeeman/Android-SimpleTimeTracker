package com.example.util.simpletimetracker.core.interactor

import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.manager.NotificationManager
import com.example.util.simpletimetracker.core.manager.NotificationParams
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.IconMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.interactor.RecordTypeInteractor
import javax.inject.Inject

class NotificationInteractor @Inject constructor(
    private val notificationManager: NotificationManager,
    private val recordTypeInteractor: RecordTypeInteractor,
    private val iconMapper: IconMapper,
    private val colorMapper: ColorMapper,
    private val resourceRepo: ResourceRepo
) {

    suspend fun showNotification(typeId: Long) {
        val recordType = recordTypeInteractor.get(typeId)

        notificationManager.show(
            NotificationParams(
                id = typeId.toInt(),
                icon = recordType?.icon
                    ?.let(iconMapper::mapToDrawableResId)
                    ?: R.drawable.unknown,
                color = recordType?.color
                    ?.let(colorMapper::mapToColorResId)
                    ?.let(resourceRepo::getColor)
                    ?: resourceRepo.getColor(R.color.black),
                text = recordType?.name
                    .orEmpty()
            )
        )
    }

    fun hideNotification(typeId: Long) {
        notificationManager.hide(typeId.toInt())
    }
}