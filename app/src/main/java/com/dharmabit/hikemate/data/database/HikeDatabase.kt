package com.dharmabit.hikemate.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.dharmabit.hikemate.data.database.entities.Hike
import com.dharmabit.hikemate.data.database.entities.TrackPoint
import com.dharmabit.hikemate.utils.Constants.DATABASE_NAME

@Database(
    entities = [Hike::class, TrackPoint::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class HikeDatabase : RoomDatabase() {

    abstract fun getHikeDao(): HikeDao

    companion object {
        @Volatile
        private var INSTANCE: HikeDatabase? = null

        fun getDatabase(context: Context): HikeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HikeDatabase::class.java,
                    DATABASE_NAME
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}