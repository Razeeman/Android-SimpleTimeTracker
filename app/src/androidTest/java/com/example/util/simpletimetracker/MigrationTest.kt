package com.example.util.simpletimetracker

import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.util.simpletimetracker.data_local.database.AppDatabase
import com.example.util.simpletimetracker.data_local.database.AppDatabaseMigrations
import java.io.IOException
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MigrationTest {
    private val testDatabaseName = "migration-test"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java.canonicalName,
        FrameworkSQLiteOpenHelperFactory(),
    )

    @Test
    @Throws(IOException::class)
    fun migrateAll() {
        // Create earliest version of the database.
        helper.createDatabase(testDatabaseName, 6)
            .apply { close() }

        // Open latest version of the database. Room validates the schema
        // once all migrations execute.
        Room
            .databaseBuilder(
                InstrumentationRegistry.getInstrumentation().targetContext,
                AppDatabase::class.java,
                testDatabaseName,
            )
            .addMigrations(
                *AppDatabaseMigrations.migrations.toTypedArray(),
            )
            .build()
            .apply { openHelper.writableDatabase.close() }
    }
}