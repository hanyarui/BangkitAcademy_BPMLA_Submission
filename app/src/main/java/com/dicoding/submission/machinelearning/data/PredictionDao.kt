package com.dicoding.submission.machinelearning.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dicoding.submission.machinelearning.local.data.Prediction

@Dao
interface PredictionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrediction(prediction: Prediction)

    @Query("SELECT * FROM prediction_history")
    suspend fun getAllPredictions(): List<Prediction>

    @Delete
    suspend fun deletePrediction(prediction: Prediction)
}