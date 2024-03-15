package jain.piyush.happyplaceapp.activities.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import jain.piyush.happyplaceapp.activities.model.HappyPlaceModel



class DatabaseHandler(private var context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "HappyPlaceDatabase"
        private const val TABLE_HAPPY_PLACE = "HappyPlaceTable"

        private const val KEY_ID = "id"
        private const val KEY_TITLE = "title"
        private const val KEY_IMAGE = "image"
        private const val KEY_DESCRIPTION = "description"
        private const val KEY_DATE = "date"
        private const val KEY_LOCATION = "location"
        private const val KEY_LATITUDE = "latitude"
        private const val KEY_LONGITUDE = "longitude"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val CREATE_HAPPY_PLACE_TABLE = ("CREATE TABLE " + TABLE_HAPPY_PLACE +
                "(" + KEY_ID + " INTEGER PRIMARY KEY, " +
                KEY_TITLE + " TEXT, " +
                KEY_IMAGE + " TEXT, " +
                KEY_DESCRIPTION + " TEXT, " +
                KEY_DATE + " TEXT, " +
                KEY_LOCATION + " TEXT, " +
                KEY_LATITUDE + " TEXT, " +
                KEY_LONGITUDE + " TEXT)")
        db?.execSQL(CREATE_HAPPY_PLACE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS $TABLE_HAPPY_PLACE")
        onCreate(db)
    }

    fun addHappyPlace(addHappyPlace: HappyPlaceModel): Long {
        val db = this.writableDatabase

        val contentValue = ContentValues()
        contentValue.put(KEY_TITLE, addHappyPlace.title)
        contentValue.put(KEY_IMAGE, addHappyPlace.image)
        contentValue.put(KEY_DESCRIPTION, addHappyPlace.description)
        contentValue.put(KEY_DATE, addHappyPlace.date)
        contentValue.put(KEY_LOCATION, addHappyPlace.location)
        contentValue.put(KEY_LATITUDE, addHappyPlace.latitude)
        contentValue.put(KEY_LONGITUDE, addHappyPlace.longitude)

        val result = db.insert(TABLE_HAPPY_PLACE, null, contentValue)
        db.close()
        return result
    }
    fun updateHappyPlace(addHappyPlace: HappyPlaceModel): Int {
        val db = this.writableDatabase

        val contentValue = ContentValues()
        contentValue.put(KEY_TITLE, addHappyPlace.title)
        contentValue.put(KEY_IMAGE, addHappyPlace.image)
        contentValue.put(KEY_DESCRIPTION, addHappyPlace.description)
        contentValue.put(KEY_DATE, addHappyPlace.date)
        contentValue.put(KEY_LOCATION, addHappyPlace.location)
        contentValue.put(KEY_LATITUDE, addHappyPlace.latitude)
        contentValue.put(KEY_LONGITUDE, addHappyPlace.longitude)

        val success = db.update(TABLE_HAPPY_PLACE, contentValue, KEY_ID + "=" + addHappyPlace.id,null)
        db.close()
        return success
    }
    fun deleteHappyPlace(happyPlace : HappyPlaceModel):Int{
        val db = this.writableDatabase
        val success = db.delete(TABLE_HAPPY_PLACE, KEY_ID + "=" + happyPlace.id,null)
        db.close()
        return success
    }

    fun getHappyPlaceList(): ArrayList<HappyPlaceModel> {
        val happyPlaceList = ArrayList<HappyPlaceModel>()
        val selectQuery = "SELECT * FROM $TABLE_HAPPY_PLACE"
        val db = this.readableDatabase
        val cursor = db.rawQuery(selectQuery, null)
        try {
            if (cursor.moveToFirst()) {
                do {
                    val place = HappyPlaceModel(
                        cursor.getInt(cursor.getColumnIndex(KEY_ID)),
                        cursor.getString(cursor.getColumnIndex(KEY_TITLE)),
                        cursor.getString(cursor.getColumnIndex(KEY_IMAGE)),
                        cursor.getString(cursor.getColumnIndex(KEY_DESCRIPTION)),
                        cursor.getString(cursor.getColumnIndex(KEY_DATE)),
                        cursor.getString(cursor.getColumnIndex(KEY_LOCATION)),
                        cursor.getDouble(cursor.getColumnIndex(KEY_LATITUDE)),
                        cursor.getDouble(cursor.getColumnIndex(KEY_LONGITUDE))

                    )
                    happyPlaceList.add(place)

                } while(cursor.moveToNext())
            }
        } catch (e: SQLiteException) {
            Log.e("DatabaseHandler", "Error while reading from database", e)
        } finally {
            cursor.close()
            db.close()
        }
        return happyPlaceList
    }
}

