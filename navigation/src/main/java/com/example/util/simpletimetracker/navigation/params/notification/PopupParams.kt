package com.example.util.simpletimetracker.navigation.params.notification

import com.example.util.simpletimetracker.domain.model.Coordinates

data class PopupParams(
    val message: String,
    val anchorCoordinates: Coordinates,
) : NotificationParams