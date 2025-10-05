package com.dharmabit.hikemate.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dharmabit.hikemate.data.database.entities.Hike
import com.dharmabit.hikemate.data.database.entities.TrackPoint
import com.dharmabit.hikemate.data.repository.HikeRepository
import kotlinx.coroutines.launch

class HikeViewModel(private val repository: HikeRepository) : ViewModel() {

    fun insertHike(hike: Hike, trackPoints: List<TrackPoint>) = viewModelScope.launch {
        val hikeId = repository.insertHike(hike)
        val updatedTrackPoints = trackPoints.map { it.copy(hikeId = hikeId) }
        repository.insertTrackPoints(updatedTrackPoints)
    }

    fun deleteHike(hike: Hike) = viewModelScope.launch {
        repository.deleteHike(hike)
    }

    suspend fun getHikeById(hikeId: Long): Hike? {
        return repository.getHikeById(hikeId)
    }

    suspend fun getTrackPointsByHikeId(hikeId: Long): List<TrackPoint> {
        return repository.getTrackPointsByHikeId(hikeId)
    }
}