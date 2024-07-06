package com.example.util.simpletimetracker.feature_dialogs.recordQuickActions.model

data class RecordQuickActionsState(
    val buttons: List<Button>,
) {

    sealed interface Button {
        val wrapBefore: Boolean

        data class Statistics(
            override val wrapBefore: Boolean,
        ) : Button

        data class Delete(
            override val wrapBefore: Boolean,
        ) : Button

        data class Continue(
            override val wrapBefore: Boolean,
        ) : Button

        data class Repeat(
            override val wrapBefore: Boolean,
        ) : Button

        data class Duplicate(
            override val wrapBefore: Boolean,
        ) : Button

        data class Merge(
            override val wrapBefore: Boolean,
        ) : Button
    }
}