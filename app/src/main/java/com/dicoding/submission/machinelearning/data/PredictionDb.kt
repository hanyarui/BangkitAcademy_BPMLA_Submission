package com.dicoding.submission.machinelearning.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.dicoding.submission.machinelearning.local.data.Prediction

@Database(entities = [Prediction::class], version = 2, exportSchema = false)
abstract class PredictionDb : RoomDatabase() {

    abstract fun predictionHistoryDao(): PredictionDao

    companion object {
        @Volatile
        private var INSTANCE: PredictionDb? = null

        fun getDatabase(context: Context): PredictionDb {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PredictionDb::class.java,
                    "app_database"
                )
                    .build()
                INSTANCE = instance
                return instance
            }
        }
    }
}