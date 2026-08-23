package com.example.util.simpletimetracker.feature_icon_selection.mapper

import androidx.annotation.ColorInt
import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.feature_icon_selection.viewData.IconSelectionCategoryInfoViewData
import com.example.util.simpletimetracker.feature_icon_selection.viewData.IconSelectionSwitchViewData
import com.example.util.simpletimetracker.core.mapper.ColorMapper
import com.example.util.simpletimetracker.core.mapper.IconEmojiMapper
import com.example.util.simpletimetracker.core.mapper.IconImageMapper
import com.example.util.simpletimetracker.core.mapper.IconMapper
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.domain.color.model.AppColor
import com.example.util.simpletimetracker.domain.favourite.model.FavouriteIcon
import com.example.util.simpletimetracker.domain.icon.IconEmoji
import com.example.util.simpletimetracker.domain.icon.IconEmojiType
import com.example.util.simpletimetracker.domain.icon.IconImage
import com.example.util.simpletimetracker.domain.icon.IconImageState
import com.example.util.simpletimetracker.domain.icon.IconImageType
import com.example.util.simpletimetracker.domain.icon.IconType
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.emoji.EmojiViewData
import com.example.util.simpletimetracker.feature_icon_selection.api.mapper.IconSelectionMapper
import com.example.util.simpletimetracker.feature_icon_selection.api.viewData.IconSelectionCategoryViewData
import com.example.util.simpletimetracker.feature_icon_selection.api.viewData.IconSelectionSelectorStateViewData
import com.example.util.simpletimetracker.feature_icon_selection.api.viewData.IconSelectionTypeViewData
import com.example.util.simpletimetracker.feature_icon_selection.api.viewData.IconSelectionViewData
import com.example.util.simpletimetracker.feature_views.viewData.RecordTypeIcon
import com.example.util.simpletimetracker.navigation.params.screen.EmojiSelectionDialogParams
import com.example.util.simpletimetracker.resources.IconMapperUtils
import javax.inject.Inject

class IconSelectionMapper @Inject constructor(
    private val resourceRepo: ResourceRepo,
    private val iconImageMapper: IconImageMapper,
    private val iconEmojiMapper: IconEmojiMapper,
    private val iconMapper: IconMapper,
    private val colorMapper: ColorMapper,
) : IconSelectionMapper {

    override fun mapFavouriteIconImages(
        favourites: List<FavouriteIcon>,
    ): List<IconImage> {
        return favourites
            .filter { IconMapperUtils.isImageIcon(it.icon) }
            .mapNotNull {
                val resId = iconMapper.mapIcon(it.icon)
                    as? RecordTypeIcon.Image
                    ?: return@mapNotNull null
                IconImage(
                    iconName = it.icon,
                    iconResId = resId.iconId,
                    iconSearch = "",
                )
            }
    }

    override fun mapFavouriteIconEmojis(
        favourites: List<FavouriteIcon>,
    ): List<IconEmoji> {
        return favourites
            .filter { !IconMapperUtils.isImageIcon(it.icon) }
            .map {
                IconEmoji(
                    emojiCode = it.icon,
                    emojiSearch = "",
                )
            }
    }

    fun mapIconImageData(
        newColor: AppColor,
        search: String,
        favourites: List<FavouriteIcon>,
        isDarkTheme: Boolean,
    ): List<ViewHolderType> {
        val isSearching = search.isNotBlank()
        val actualSearch = search.lowercase().split(" ")
        val favouriteIconImages = favourites
            .takeUnless { isSearching }
            .orEmpty()
            .let(::mapFavouriteIconImages)
        val iconCategories = iconImageMapper.getAvailableImages(
            loadSearchHints = isSearching,
        )

        return iconCategories.toList().mapIndexed { index, (category, images) ->
            val categoryImages = if (category.type == IconImageType.FAVOURITES) {
                favouriteIconImages
            } else {
                images
            }

            if (categoryImages.isEmpty()) return@mapIndexed emptyList()

            val categoryViewData = IconSelectionCategoryInfoViewData(
                type = IconSelectionTypeViewData.Image(category.type, index.toLong()),
                text = category.name,
                isLast = index == iconCategories.size - 1,
            )
                .let(::listOf)
                .takeIf { !isSearching } // Don't show category on search.
                .orEmpty()

            val iconsViewData = categoryImages.mapNotNull {
                if (isSearching) {
                    val iconName = it.iconName.prepareIconName()
                    if (!containsSearch(actualSearch, iconName, it.iconSearch)) {
                        return@mapNotNull null
                    }
                }

                mapImageViewData(
                    iconName = it.iconName,
                    iconResId = it.iconResId,
                    newColor = newColor.let { colorMapper.mapToColorInt(it, isDarkTheme) },
                )
            }

            categoryViewData + iconsViewData
        }.flatten()
    }

    fun mapIconEmojiData(
        newColor: AppColor,
        search: String,
        favourites: List<FavouriteIcon>,
        isDarkTheme: Boolean,
    ): List<ViewHolderType> {
        val isSearching = search.isNotBlank()
        val actualSearch = search.lowercase().split(" ")
        val favouriteEmojiTexts = favourites
            .takeUnless { isSearching }
            .orEmpty()
            .let(::mapFavouriteIconEmojis)
        val iconCategories = iconEmojiMapper.getAvailableEmojis(
            loadSearchHints = isSearching,
        )
        return iconCategories.toList().mapIndexed { index, (category, codes) ->
            val categoryCodes = if (category.type == IconEmojiType.FAVOURITES) {
                favouriteEmojiTexts
            } else {
                codes
            }

            if (categoryCodes.isEmpty()) return@mapIndexed emptyList()

            val categoryViewData = IconSelectionCategoryInfoViewData(
                type = IconSelectionTypeViewData.Emoji(category.type, index.toLong()),
                text = category.name,
                isLast = index == iconCategories.size - 1,
            )
                .let(::listOf)
                .takeIf { !isSearching } // Don't show category on search.
                .orEmpty()

            val codesViewData = categoryCodes.mapNotNull {
                if (isSearching) {
                    if (!containsSearch(actualSearch, it.emojiSearch)) {
                        return@mapNotNull null
                    }
                }

                mapEmojiViewData(
                    codes = it.emojiCode,
                    newColor = newColor.let { colorMapper.mapToColorInt(it, isDarkTheme) },
                )
            }

            categoryViewData + codesViewData
        }.flatten()
    }

    fun mapIconImageCategories(
        selectedIndex: Long,
        hasFavourites: Boolean,
    ): List<ViewHolderType> {
        val categories = iconImageMapper.getAvailableCategories(true)
        return categories.mapIndexedNotNull { index, iconImageCategory ->
            if (iconImageCategory.type == IconImageType.FAVOURITES && !hasFavourites) {
                return@mapIndexedNotNull null
            }
            IconSelectionCategoryViewData(
                type = IconSelectionTypeViewData.Image(
                    type = iconImageCategory.type,
                    id = index.toLong(),
                ),
                categoryIcon = iconImageCategory.categoryIcon,
                selected = selectedIndex == index.toLong(),
            )
        }
    }

    fun mapIconEmojiCategories(
        selectedIndex: Long,
        hasFavourites: Boolean,
    ): List<ViewHolderType> {
        val categories = iconEmojiMapper.getAvailableEmojiCategories(true)
        return categories.mapIndexedNotNull { index, iconEmojiCategory ->
            if (iconEmojiCategory.type == IconEmojiType.FAVOURITES && !hasFavourites) {
                return@mapIndexedNotNull null
            }
            IconSelectionCategoryViewData(
                type = IconSelectionTypeViewData.Emoji(
                    type = iconEmojiCategory.type,
                    id = index.toLong(),
                ),
                categoryIcon = iconEmojiCategory.categoryIcon,
                selected = selectedIndex == index.toLong(),
            )
        }
    }

    fun mapToIconSwitchViewData(iconType: IconType): List<ViewHolderType> {
        return listOf(
            IconType.IMAGE,
            IconType.TEXT,
            IconType.EMOJI,
        ).map {
            IconSelectionSwitchViewData(
                iconType = it,
                name = mapToFilterTypeName(it),
                isSelected = it == iconType,
            )
        }
    }

    fun mapToIconSelectorViewData(
        iconImageState: IconImageState,
        iconType: IconType,
        isSelectedIconFavourite: Boolean,
        isDarkTheme: Boolean,
    ): IconSelectionSelectorStateViewData {
        return if (iconType == IconType.TEXT) {
            IconSelectionSelectorStateViewData.None
        } else {
            IconSelectionSelectorStateViewData.Available(
                state = iconImageState,
                searchButtonColor = when (iconImageState) {
                    is IconImageState.Chooser -> R.attr.appInactiveColor
                    is IconImageState.Search -> R.attr.colorSecondary
                }.let { resourceRepo.getThemedAttr(it, isDarkTheme) },
                favouriteButtonColor = if (isSelectedIconFavourite) {
                    R.attr.colorSecondary
                } else {
                    R.attr.appInactiveColor
                }.let { resourceRepo.getThemedAttr(it, isDarkTheme) },
            )
        }
    }

    fun mapEmojiSelectionParams(
        tag: String,
        color: AppColor,
        emojiCodes: String,
    ): EmojiSelectionDialogParams {
        return EmojiSelectionDialogParams(
            tag = tag,
            color = EmojiSelectionDialogParams.Color(
                colorId = color.colorId,
                colorInt = color.colorInt,
            ),
            emojiCodes = listOf(emojiCodes) + iconEmojiMapper.toSkinToneVariations(emojiCodes),
        )
    }

    private fun mapToFilterTypeName(iconType: IconType): String {
        return when (iconType) {
            IconType.IMAGE -> R.string.change_record_type_icon_image_hint
            IconType.TEXT -> R.string.change_record_type_icon_text_hint
            IconType.EMOJI -> R.string.change_record_type_icon_emoji_hint
        }.let(resourceRepo::getString)
    }

    override fun mapImageViewData(
        iconName: String,
        iconResId: Int,
        @ColorInt newColor: Int,
    ): ViewHolderType {
        return IconSelectionViewData(
            iconName = iconName,
            iconResId = iconResId,
            colorInt = newColor,
        )
    }

    override fun mapEmojiViewData(
        codes: String,
        @ColorInt newColor: Int,
    ): ViewHolderType {
        return EmojiViewData(
            emojiText = iconEmojiMapper.toEmojiString(codes),
            emojiCodes = codes,
            colorInt = newColor,
        )
    }

    // If search text is several words - all words should be found.
    private fun containsSearch(
        searchParts: List<String>,
        vararg searchableField: String,
    ): Boolean {
        return searchParts.all { part ->
            searchableField.any { it.contains(part) }
        }
    }

    private fun String.prepareIconName(): String {
        return this.removePrefix("ic_").removeSuffix("_24px")
    }
}