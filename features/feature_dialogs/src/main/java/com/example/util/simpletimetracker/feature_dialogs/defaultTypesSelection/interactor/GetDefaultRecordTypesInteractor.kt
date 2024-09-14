package com.example.util.simpletimetracker.feature_dialogs.defaultTypesSelection.interactor

import com.example.util.simpletimetracker.domain.model.AppColor
import com.example.util.simpletimetracker.domain.model.RecordType
import javax.inject.Inject

class GetDefaultRecordTypesInteractor @Inject constructor() {

    fun execute(): List<RecordType> {
        return defaultTypes
            .mapIndexed { index, type ->
                RecordType(
                    // Add ids for recycler, will be removed later before adding to DB.
                    id = index.toLong(),
                    name = type.name,
                    icon = type.icon,
                    color = AppColor(colorId = type.colorId, colorInt = ""),
                    defaultDuration = 0L,
                    note = "",
                )
            }
    }

    private val defaultTypes: List<DefaultRecordType> by lazy {
        listOf(
            DefaultRecordType(name = "Games", icon = "ic_headset_24px", colorId = 1),
            DefaultRecordType(name = "Tv", icon = "ic_desktop_windows_24px", colorId = 1),
            DefaultRecordType(name = "Youtube", icon = "ic_ondemand_video_24px", colorId = 1),

            DefaultRecordType(name = "Exercise", icon = "ic_fitness_center_24px", colorId = 3),
            DefaultRecordType(name = "Meditate", icon = "ic_lightbulb_outline_24px", colorId = 3),
            DefaultRecordType(name = "Read", icon = "ic_import_contacts_24px", colorId = 3),

            DefaultRecordType(name = "Chores", icon = "ic_assignment_24px", colorId = 5),
            DefaultRecordType(name = "Cleaning", icon = "ic_delete_24px", colorId = 5),
            DefaultRecordType(name = "Outdoors", icon = "ic_directions_walk_24px", colorId = 5),
            DefaultRecordType(name = "Indoors", icon = "ic_extension_24px", colorId = 5),
            DefaultRecordType(name = "Shopping", icon = "ic_shopping_cart_24px", colorId = 5),

            DefaultRecordType(name = "Cooking", icon = "ic_restaurant_menu_24px", colorId = 7),
            DefaultRecordType(name = "Breakfast", icon = "ic_free_breakfast_24px", colorId = 7),
            DefaultRecordType(name = "Lunch", icon = "ic_restaurant_24px", colorId = 7),
            DefaultRecordType(name = "Dinner", icon = "ic_local_bar_24px", colorId = 7),

            DefaultRecordType(name = "Sleep", icon = "ic_airline_seat_individual_suite_24px", colorId = 10),
            DefaultRecordType(name = "Commute", icon = "ic_airport_shuttle_24px", colorId = 10),
            DefaultRecordType(name = "Work", icon = "ic_business_center_24px", colorId = 10),
        )
    }

    private data class DefaultRecordType(
        val name: String,
        val icon: String,
        val colorId: Int,
    )
}