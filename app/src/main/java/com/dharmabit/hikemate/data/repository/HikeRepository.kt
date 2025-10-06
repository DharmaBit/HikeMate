package com.dharmabit.hikemate.data.repository

import androidx.lifecycle.LiveData
import com.dharmabit.hikemate.data.database.HikeDao
import com.dharmabit.hikemate.data.database.entities.Hike
import com.dharmabit.hikemate.data.database.entities.TrackPoint
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HikeRepository @Inject constructor(
    private val hikeDao: HikeDao
) {

    suspend fun insertHike(hike: Hike): Long = hikeDao.insertHike(hike)

    suspend fun insertTrackPoints(trackPoints: List<TrackPoint>) = hikeDao.insertTrackPoints(trackPoints)

    suspend fun updateHike(hike: Hike) = hikeDao.updateHike(hike)

    suspend fun deleteHike(hike: Hike) {
        hikeDao.deleteTrackPointsByHikeId(hike.id)
        hikeDao.deleteHike(hike)
    }

    fun getAllHikes(): LiveData<List<Hike>> = hikeDao.getAllHikes()

    suspend fun getHikeById(hikeId: Long): Hike? = hikeDao.getHikeById(hikeId)

    suspend fun getTrackPointsByHikeId(hikeId: Long): List<TrackPoint> = hikeDao.getTrackPointsByHikeId(hikeId)

    suspend fun getTotalDistance(): Float = hikeDao.getTotalDistance() ?: 0f

    suspend fun getTotalDuration(): Long = hikeDao.getTotalDuration() ?: 0L

    suspend fun getHikeCount(): Int = hikeDao.getHikeCount()

    suspend fun getTotalElevationGain(): Float = hikeDao.getTotalElevationGain() ?: 0f

    suspend fun getTotalCalories(): Int = hikeDao.getTotalCalories() ?: 0

    suspend fun getLongestHike(): Hike? = hikeDao.getLongestHike()

    suspend fun getMaxSpeed(): Float = hikeDao.getMaxSpeed() ?: 0f
}