package com.example.util.simpletimetracker.data_local.recordsFilter

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.example.util.simpletimetracker.data_local.recordType.RecordTypeGoalDBO

data class FavouriteRecordsFilterDBO(
    @Embedded
    val main: MainDBO,

    @Relation(
        entity = FilterDBO::class,
        parentColumn = "id",
        entityColumn = "owner_id",
    )
    val filters: List<FilterDBO>,
) {

    @Entity(tableName = "favouriteRecordFilters")
    data class MainDBO(
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "id")
        val id: Long,
    )

    @Entity(
        tableName = "favouriteRecordFilter",
        foreignKeys = [
            ForeignKey(
                entity = MainDBO::class,
                parentColumns = ["id"],
                childColumns = ["owner_id"],
                onDelete = ForeignKey.CASCADE,
            ),
        ],
    )
    data class FilterDBO(
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "id")
        val id: Long,

        @ColumnInfo(name = "owner_id")
        val ownerId: Long,

        @ColumnInfo(name = "type")
        val type: Long,

        // Longs stored in string comma separated
        @ColumnInfo(name = "common_items_ids")
        val commonItemsIds: String?,

        // Ids stored in string comma separated
        @ColumnInfo(name = "comment_items_ids")
        val commentItemsIds: String?,

        @ColumnInfo(name = "comment_items_text")
        val commentItemsText: String?,

        // Ids stored in string comma separated
        @ColumnInfo(name = "duplication_items_ids")
        val duplicationItemsIds: String?,

        // Ids stored in string comma separated
        @ColumnInfo(name = "manually_filtered_items_ids")
        val manuallyFilteredItemsIds: String?,

        @Embedded(prefix = "range_")
        val range: RangeDBO?,

        @Embedded(prefix = "range_length_")
        val rangeLength: RangeLengthDBO?,

        /**
         * How data is stored - see [RecordTypeGoalDBO].
         */
        @ColumnInfo(name = "daysOfWeek")
        val daysOfWeek: String?,
    )

    data class RangeDBO(
        @ColumnInfo(name = "time_started")
        val rangeTimeStarted: Long,

        @ColumnInfo(name = "time_ended")
        val rangeTimeEnded: Long,
    )

    data class RangeLengthDBO(
        @ColumnInfo(name = "type")
        val rangeType: Long,

        @Embedded(prefix = "custom_range_")
        val customRange: RangeDBO?,

        @ColumnInfo(name = "last_days")
        val lastDays: Long?,

        @ColumnInfo(name = "position")
        val position: Long,
    )
}