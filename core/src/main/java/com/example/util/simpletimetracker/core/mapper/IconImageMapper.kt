package com.example.util.simpletimetracker.core.mapper

import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.repo.IconImageRepo
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.model.IconImage
import com.example.util.simpletimetracker.domain.model.IconImageCategory
import com.example.util.simpletimetracker.domain.model.IconImageType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IconImageMapper @Inject constructor(
    private val repo: IconImageRepo,
    private val resourceRepo: ResourceRepo,
) {

    fun getAvailableCategories(hasFavourites: Boolean): List<IconImageCategory> = listOfNotNull(
        IconImageCategory(
            type = IconImageType.FAVOURITES,
            name = resourceRepo.getString(R.string.change_record_favourite_comments_hint),
            categoryIcon = R.drawable.icon_category_image_favourite,
        ).takeIf { hasFavourites },
        IconImageCategory(
            type = IconImageType.MAPS,
            name = resourceRepo.getString(R.string.imageGroupMaps),
            categoryIcon = R.drawable.icon_category_image_maps,
        ),
        IconImageCategory(
            type = IconImageType.PLACES,
            name = resourceRepo.getString(R.string.imageGroupPlaces),
            categoryIcon = R.drawable.icon_category_image_places,
        ),
        IconImageCategory(
            type = IconImageType.SOCIAL,
            name = resourceRepo.getString(R.string.imageGroupSocial),
            categoryIcon = R.drawable.icon_category_image_social,
        ),
        IconImageCategory(
            type = IconImageType.ACTION,
            name = resourceRepo.getString(R.string.imageGroupAction),
            categoryIcon = R.drawable.icon_category_image_action,
        ),
        IconImageCategory(
            type = IconImageType.HARDWARE,
            name = resourceRepo.getString(R.string.imageGroupHardware),
            categoryIcon = R.drawable.icon_category_image_hardware,
        ),
        IconImageCategory(
            type = IconImageType.ALERT,
            name = resourceRepo.getString(R.string.imageGroupAlert),
            categoryIcon = R.drawable.icon_category_image_alert,
        ),
        IconImageCategory(
            type = IconImageType.AV,
            name = resourceRepo.getString(R.string.imageGroupAv),
            categoryIcon = R.drawable.icon_category_image_av,
        ),
        IconImageCategory(
            type = IconImageType.COMMUNICATION,
            name = resourceRepo.getString(R.string.imageGroupCommunication),
            categoryIcon = R.drawable.icon_category_image_communication,
        ),
        IconImageCategory(
            type = IconImageType.CONTENT,
            name = resourceRepo.getString(R.string.imageGroupContent),
            categoryIcon = R.drawable.icon_category_image_content,
        ),
        IconImageCategory(
            type = IconImageType.DEVICE,
            name = resourceRepo.getString(R.string.imageGroupDevice),
            categoryIcon = R.drawable.icon_category_image_device,
        ),
        IconImageCategory(
            type = IconImageType.EDITOR,
            name = resourceRepo.getString(R.string.imageGroupEditor),
            categoryIcon = R.drawable.icon_category_image_editor,
        ),
        IconImageCategory(
            type = IconImageType.FILE,
            name = resourceRepo.getString(R.string.imageGroupFile),
            categoryIcon = R.drawable.icon_category_image_file,
        ),
        IconImageCategory(
            type = IconImageType.IMAGE,
            name = resourceRepo.getString(R.string.imageGroupImage),
            categoryIcon = R.drawable.icon_category_image_image,
        ),
        IconImageCategory(
            type = IconImageType.NAVIGATION,
            name = resourceRepo.getString(R.string.imageGroupNavigation),
            categoryIcon = R.drawable.icon_category_image_navigation,
        ),
        IconImageCategory(
            type = IconImageType.NOTIFICATION,
            name = resourceRepo.getString(R.string.imageGroupNotification),
            categoryIcon = R.drawable.icon_category_image_notification,
        ),
        IconImageCategory(
            type = IconImageType.TOGGLE,
            name = resourceRepo.getString(R.string.imageGroupToggle),
            categoryIcon = R.drawable.icon_category_image_toggle,
        ),
    )

    fun getAvailableImages(
        loadSearchHints: Boolean,
    ): Map<IconImageCategory, List<IconImage>> {
        return getAvailableCategories(true).associateWith {
            val images = mapTypeToIconArray(it.type)
                ?.let(repo::getImages).orEmpty()
            val searchHints = if (loadSearchHints) {
                mapTypeToSearchArray(it.type)
                    ?.let(resourceRepo::getStringArray).orEmpty()
            } else {
                emptyList()
            }

            images.keys.mapIndexedNotNull { index, iconName ->
                IconImage(
                    iconName = iconName,
                    iconResId = images[iconName] ?: return@mapIndexedNotNull null,
                    iconSearch = searchHints.getOrNull(index).orEmpty(),
                )
            }
        }
    }

    private fun mapTypeToIconArray(type: IconImageType): Int? = when (type) {
        IconImageType.FAVOURITES -> null
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

    private fun mapTypeToSearchArray(type: IconImageType): Int? = when (type) {
        IconImageType.FAVOURITES -> null
        IconImageType.MAPS -> R.array.icon_hint_maps
        IconImageType.PLACES -> R.array.icon_hint_places
        IconImageType.SOCIAL -> R.array.icon_hint_social
        IconImageType.ACTION -> R.array.icon_hint_action
        IconImageType.HARDWARE -> R.array.icon_hint_hardware
        IconImageType.ALERT -> R.array.icon_hint_alert
        IconImageType.AV -> R.array.icon_hint_av
        IconImageType.COMMUNICATION -> R.array.icon_hint_communication
        IconImageType.CONTENT -> R.array.icon_hint_content
        IconImageType.DEVICE -> R.array.icon_hint_device
        IconImageType.EDITOR -> R.array.icon_hint_editor
        IconImageType.FILE -> R.array.icon_hint_file
        IconImageType.IMAGE -> R.array.icon_hint_image
        IconImageType.NAVIGATION -> R.array.icon_hint_navigation
        IconImageType.NOTIFICATION -> R.array.icon_hint_notification
        IconImageType.TOGGLE -> R.array.icon_hint_toggle
    }
}