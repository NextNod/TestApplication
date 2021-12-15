package com.example.testapplication

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.room.*
import androidx.room.Delete
import androidx.room.Update
import androidx.room.Dao
import androidx.room.RoomDatabase
import androidx.room.Database
import androidx.room.OnConflictStrategy.REPLACE
import java.io.ByteArrayOutputStream

@Dao
interface PhotoDao {
    @Query("SELECT * FROM Photo")
    fun all(): List<Photo?>?

    @Query("SELECT * FROM Photo WHERE id = :id")
    fun getById(id: Long): Photo?

    @Insert(onConflict = REPLACE)
    fun insert(employee: Photo?)

    @Update
    fun update(employee: Photo?)

    @Delete
    fun delete(employee: Photo?)
}

class Converters {

    @TypeConverter
    fun toBitmap(bytes: ByteArray): Bitmap {
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    @TypeConverter
    fun fromBitmap(bmp: Bitmap): ByteArray {
        val outputStream = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        return outputStream.toByteArray()
    }
}

@Database(entities = [Photo::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): PhotoDao?
}