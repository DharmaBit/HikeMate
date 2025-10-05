package com.dharmabit.hikemate.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dharmabit.hikemate.data.repository.HikeRepository
import kotlinx.coroutines.async

class StatsViewModel(private val repository: HikeRepository) : ViewModel() {

    suspend fun getTotalDistance() = viewModelScope.async {
        repository.getTotalDistance()
    }.await()

    suspend fun getTotalDuration() = viewModelScope.async {
        repository.getTotalDuration()
    }.await()

    suspend fun getHikeCount() = viewModelScope.async {
        repository.getHikeCount()
    }.await()

    suspend fun getTotalElevationGain() = viewModelScope.async {
        repository.getTotalElevationGain()
    }.await()

    suspend fun getTotalCalories() = viewModelScope.async {
        repository.getTotalCalories()
    }.await()

    suspend fun getLongestHike() = viewModelScope.async {
        repository.getLongestHike()
    }.await()

    suspend fun getMaxSpeed() = viewModelScope.async {
        repository.getMaxSpeed()
    }.await()
}