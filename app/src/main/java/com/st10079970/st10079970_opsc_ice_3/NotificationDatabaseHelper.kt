package com.st10079970.st10079970_opsc_ice_3

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class NotificationDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "notifications.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_NAME = "notifications"
        private const val COLUMN_ID = "id"
        private const val COLUMN_CONTENT = "content"

        private const val CREATE_TABLE = ("CREATE TABLE $TABLE_NAME (" +
                "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COLUMN_CONTENT TEXT)")
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun addNotification(content: String) {
        val db = writableDatabase
        db.execSQL("INSERT INTO $TABLE_NAME ($COLUMN_CONTENT) VALUES ('$content')")
        db.close()
    }

    fun getAllNotifications(): List<String> {
        val notifications = mutableListOf<String>()
        val db = readableDatabase
        val cursor: Cursor? = db.query(
            TABLE_NAME, // Table name
            null, // Columns (null to get all columns)
            null, // Selection
            null, // Selection args
            null, // Group by
            null, // Having
            null // Order by
        )

        cursor?.let {
            val contentIndex = it.getColumnIndex(COLUMN_CONTENT)

            if (contentIndex != -1) {
                while (it.moveToNext()) {
                    val content = it.getString(contentIndex)
                    notifications.add(content)
                }
            } else {
                Log.e("Database", "Column index for $COLUMN_CONTENT is invalid")
            }
            it.close()
        } ?: Log.e("Database", "Cursor is null or query failed")

        db.close()
        return notifications
    }
}