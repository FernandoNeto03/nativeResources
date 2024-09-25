package com.example.recursosnativos

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context : Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        const val DATABASE_NAME = "images.db"
        const val DATABASE_VERSION = 1
        const val TABLE_NAME = "images"
        const val COLUMN_ID = "id"
        const val COLUMN_IMAGE = "image"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTable = "CREATE TABLE $TABLE_NAME (" +
                "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "$COLUMN_IMAGE BLOB)"
        db?.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun insertImage(imageBytes: ByteArray): Long {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_IMAGE, imageBytes)
        }
        return db.insert(TABLE_NAME, null, contentValues)
    }

    fun getImage(id: Long): ByteArray? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_NAME, arrayOf(COLUMN_IMAGE), "$COLUMN_ID=?",
            arrayOf(id.toString()), null, null, null
        )
        cursor?.moveToFirst()
        val imageBytes = cursor?.getBlob(0)
        cursor?.close()

        return imageBytes
    }
}