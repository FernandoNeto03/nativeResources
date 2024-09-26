package com.example.recursosnativos

import android.content.Context
import android.database.Cursor


class DatabaseHelper(context: Context) : android.database.sqlite.SQLiteOpenHelper(context, "formdata.db", null, 1) {

    override fun onCreate(db: android.database.sqlite.SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE FormData (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT," +
                    "email TEXT," +
                    "comment TEXT," +
                    "image_path TEXT)"
        )
    }

    override fun onUpgrade(db: android.database.sqlite.SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS FormData")
        onCreate(db)
    }

    fun getAllFormData(): List<FormData> {
        val formDataList = mutableListOf<FormData>()
        val db = readableDatabase
        val cursor: Cursor = db.query("FormData", null, null, null, null, null, null)
        if (cursor.moveToFirst()) {
            do {
                val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
                val email = cursor.getString(cursor.getColumnIndexOrThrow("email"))
                val comment = cursor.getString(cursor.getColumnIndexOrThrow("comment"))
                val imagePath = cursor.getString(cursor.getColumnIndexOrThrow("image_path"))
                formDataList.add(FormData(name, email, comment, imagePath))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return formDataList
    }
}

data class FormData(
    val name: String,
    val email: String,
    val comment: String,
    val imagePath: String?
)