package com.example.util.simpletimetracker.core.mapper

import android.content.Context
import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.repo.IconImageRepo
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.model.IconImageCategory
import com.example.util.simpletimetracker.domain.model.IconImageType
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IconImageMapper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repo: IconImageRepo,
    private val resourceRepo: ResourceRepo
) {

    fun getAvailableCategories(): List<IconImageCategory> = listOf(
        IconImageCategory(
            type = IconImageType.MAPS,
            name = resourceRepo.getString(R.string.imageGroupMaps),
            categoryIcon = R.drawable.icon_category_image_maps
        ),
        IconImageCategory(
            type = IconImageType.PLACES,
            name = resourceRepo.getString(R.string.imageGroupPlaces),
            categoryIcon = R.drawable.icon_category_image_places
        ),
        IconImageCategory(
            type = IconImageType.SOCIAL,
            name = resourceRepo.getString(R.string.imageGroupSocial),
            categoryIcon = R.drawable.icon_category_image_social
        ),
        IconImageCategory(
            type = IconImageType.ACTION,
            name = resourceRepo.getString(R.string.imageGroupAction),
            categoryIcon = R.drawable.icon_category_image_action
        ),
        IconImageCategory(
            type = IconImageType.HARDWARE,
            name = resourceRepo.getString(R.string.imageGroupHardware),
            categoryIcon = R.drawable.icon_category_image_hardware
        ),
        IconImageCategory(
            type = IconImageType.ALERT,
            name = resourceRepo.getString(R.string.imageGroupAlert),
            categoryIcon = R.drawable.icon_category_image_alert
        ),
        IconImageCategory(
            type = IconImageType.AV,
            name = resourceRepo.getString(R.string.imageGroupAv),
            categoryIcon = R.drawable.icon_category_image_av
        ),
        IconImageCategory(
            type = IconImageType.COMMUNICATION,
            name = resourceRepo.getString(R.string.imageGroupCommunication),
            categoryIcon = R.drawable.icon_category_image_communication
        ),
        IconImageCategory(
            type = IconImageType.CONTENT,
            name = resourceRepo.getString(R.string.imageGroupContent),
            categoryIcon = R.drawable.icon_category_image_content
        ),
        IconImageCategory(
            type = IconImageType.DEVICE,
            name = resourceRepo.getString(R.string.imageGroupDevice),
            categoryIcon = R.drawable.icon_category_image_device
        ),
        IconImageCategory(
            type = IconImageType.EDITOR,
            name = resourceRepo.getString(R.string.imageGroupEditor),
            categoryIcon = R.drawable.icon_category_image_editor
        ),
        IconImageCategory(
            type = IconImageType.FILE,
            name = resourceRepo.getString(R.string.imageGroupFile),
            categoryIcon = R.drawable.icon_category_image_file
        ),
        IconImageCategory(
            type = IconImageType.IMAGE,
            name = resourceRepo.getString(R.string.imageGroupImage),
            categoryIcon = R.drawable.icon_category_image_image
        ),
        IconImageCategory(
            type = IconImageType.NAVIGATION,
            name = resourceRepo.getString(R.string.imageGroupNavigation),
            categoryIcon = R.drawable.icon_category_image_navigation
        ),
        IconImageCategory(
            type = IconImageType.NOTIFICATION,
            name = resourceRepo.getString(R.string.imageGroupNotification),
            categoryIcon = R.drawable.icon_category_image_notification
        ),
        IconImageCategory(
            type = IconImageType.TOGGLE,
            name = resourceRepo.getString(R.string.imageGroupToggle),
            categoryIcon = R.drawable.icon_category_image_toggle
        )
    )

    fun getAvailableImages(): Map<IconImageCategory, Map<String, Int>> =
        getAvailableCategories()
            .map { it to mapTypeToArray(it.type).let(repo::getImages) }
            .toMap()

    fun mapToDrawableResId(iconName: String): Int {
        return context.resources
            .getIdentifier(iconName, "drawable", context.packageName)
            .takeIf { it != 0 }
            ?: R.drawable.unknown
    }

    private fun mapTypeToArray(type: IconImageType): Int = when (type) {
        IconImageType.MAPS -> R.array.icon_maps
        IconImageType.PLACES -> R.array.icon_places
        IconImageType.SOCIAL -> R.array.icon_social
        IconImageType.ACTION -> R.array.icon_action
        IconImageType.HARDWARE -> R.array.icon_hardware
        IconImageType.ALERT -> R.array.icon_alert
        IconImageType.AV -> R.array.icon_av
        IconImageType.COMMUNICATION -> R.array.icon_communication
        IconImageType.CONTENT -> R.array.icon_content
        IconImageType.DEVICE -> R.array.icon_device
        IconImageType.EDITOR -> R.array.icon_editor
        IconImageType.FILE -> R.array.icon_file
        IconImageType.IMAGE -> R.array.icon_image
        IconImageType.NAVIGATION -> R.array.icon_navigation
        IconImageType.NOTIFICATION -> R.array.icon_notification
        IconImageType.TOGGLE -> R.array.icon_toggle
    }
}