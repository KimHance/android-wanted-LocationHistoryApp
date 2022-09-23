package com.preonboarding.locationhistory.data.database

import android.content.Context
import androidx.room.*
import com.preonboarding.locationhistory.data.entity.History

@Database(
    entities = [History::class],
    version = 1,
    exportSchema = false
)
abstract class MapDatabase : RoomDatabase() {
    abstract fun mapDao(): MapDao

    companion object {
        fun getInstance(context: Context): MapDatabase = Room
            .databaseBuilder(context, MapDatabase::class.java, "map.db")
            .build()
    }
}

@Dao
interface MapDao {

    @Query("SELECT * FROM history WHERE date =:date")
    suspend fun getHistoryFromDate(date: String): List<History>

    @Insert
    suspend fun saveHistory(history: History)
}

