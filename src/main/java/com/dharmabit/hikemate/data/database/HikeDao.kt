package com.dharmabit.hikemate.data.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.dharmabit.hikemate.data.database.entities.Hike
import com.dharmabit.hikemate.data.database.entities.TrackPoint

@Dao
interface HikeDao {

    @Insert
    suspend fun insertHike(hike: Hike): Long

    @Insert
    suspend fun insertTrackPoints(trackPoints: List<TrackPoint>)

    @Update
    suspend fun updateHike(hike: Hike)

    @Delete
    suspend fun deleteHike(hike: Hike)

    @Query("DELETE FROM track_point_table WHERE hikeId = :hikeId")
    suspend fun deleteTrackPointsByHikeId(hikeId: Long)

    @Query("SELECT * FROM hike_table ORDER BY date DESC")
    fun getAllHikes(): LiveData<List<Hike>>

    @Query("SELECT * FROM hike_table WHERE id = :hikeId")
    suspend fun getHikeById(hikeId: Long): Hike?

    @Query("SELECT * FROM track_point_table WHERE hikeId = :hikeId ORDER BY timestamp ASC")
    suspend fun getTrackPointsByHikeId(hikeId: Long): List<TrackPoint>

    @Query("SELECT SUM(distance) FROM hike_table")
    suspend fun getTotalDistance(): Float?

    @Query("SELECT SUM(duration) FROM hike_table")
    suspend fun getTotalDuration(): Long?

    @Query("SELECT COUNT(*) FROM hike_table")
    suspend fun getHikeCount(): Int

    @Query("SELECT SUM(elevationGain) FROM hike_table")
    suspend fun getTotalElevationGain(): Float?

    @Query("SELECT SUM(calories) FROM hike_table")
    suspend fun getTotalCalories(): Int?

    @Query("SELECT * FROM hike_table ORDER BY distance DESC LIMIT 1")
    suspend fun getLongestHike(): Hike?

    @Query("SELECT MAX(maxSpeed) FROM hike_table")
    suspend fun getMaxSpeed(): Float?
}