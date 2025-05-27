package com.example.recipehub.Adpter

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.recipehub.modle.Recipe

class DataBaseHelper(val context:Context):SQLiteOpenHelper(context, DATABASE_NAME,null, version) {

    companion object{
        const val DATABASE_NAME = "RecipeHub"
        const val version = 1
        const val TABLE_NAME = "Recipes"
        const val column_ID= "id"
        const val column_RID = "rid"
        const val column_TITLE = "title"
        const val column_DESCRIPTION = "description"
        const val column_INGREDIENTS = "INGREDIENTS"
        const val column_IMAGE = "image"
    }

    override fun onCreate(p0: SQLiteDatabase?) {
       val query = "CREATE TABLE $TABLE_NAME($column_ID INTEGER PRIMARY KEY AUTOINCREMENT,$column_RID TEXT,$column_TITLE TEXT,$column_IMAGE TEXT,$column_DESCRIPTION TEXT,$column_INGREDIENTS TEXT);"
        if (p0 != null) {
            p0.execSQL(query)
        }
    }

    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {
        p0?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
    }

    fun insertData(recipe:Recipe):Long{
        val db = writableDatabase
        val content = ContentValues()
        content.put(column_RID,recipe._id)
        content.put(column_TITLE,recipe.title)
        content.put(column_IMAGE,recipe.image)
        content.put(column_DESCRIPTION,recipe.description)
        content.put(column_INGREDIENTS,recipe.ingredients)
        val a = db.insert(TABLE_NAME,null,content)
        return a
    }

    fun findImposter(rid: String): Boolean {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_NAME,
            arrayOf(column_RID),
            "$column_RID = ?",
            arrayOf(rid),
            null, null, null
        )

        val isImposter = cursor.count > 0 // If count > 0, `rid` already exists
        cursor.close()
        return isImposter
    }

    @SuppressLint("Range")
    fun fetchAll():List<Recipe>{
        val db = readableDatabase
        val cursore = db.query(TABLE_NAME, arrayOf(column_ID, column_RID, column_TITLE, column_IMAGE,
            column_DESCRIPTION, column_INGREDIENTS),null,null,null,null,null)
        var list = mutableListOf<Recipe>()
        while(cursore.moveToNext()){
            val recipe = Recipe(
                _id = cursore.getString(cursore.getColumnIndex(column_RID)),
                userId = null,
                title = cursore.getString(cursore.getColumnIndex(column_TITLE)),
                image = cursore.getString(cursore.getColumnIndex(column_IMAGE)),
                description = cursore.getString(cursore.getColumnIndex(column_DESCRIPTION)),
                ingredients = cursore.getString(cursore.getColumnIndex(column_INGREDIENTS)),
                createdAt = null,
                updatedAt = null
            )
            list.add(recipe)
        }
        return list
    }

    fun deleteByRid(rid: String): Int {
        val db = writableDatabase
        return db.delete(TABLE_NAME, "$column_RID = ?", arrayOf(rid))
    }

}