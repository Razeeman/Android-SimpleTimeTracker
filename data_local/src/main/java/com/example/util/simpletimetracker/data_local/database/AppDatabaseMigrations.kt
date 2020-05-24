package com.example.util.simpletimetracker.data_local.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

class AppDatabaseMigrations {

    companion object {
        val migration_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create the new table
                database.execSQL(
                    "CREATE TABLE recordTypes_new (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL, icon TEXT NOT NULL, color INTEGER NOT NULL, hidden INTEGER NOT NULL)"
                )

                // Copy the data
                database.execSQL(
                    "INSERT INTO recordTypes_new (id, name, icon, color, hidden) SELECT id, name, '', color, hidden FROM recordTypes"
                )

                // Remove the old table
                database.execSQL("DROP TABLE recordTypes")

                // Change the table name to the correct one
                database.execSQL("ALTER TABLE recordTypes_new RENAME TO recordTypes")
            }
        }
    }
}