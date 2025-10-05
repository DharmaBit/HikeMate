package com.dharmabit.hikemate.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "hike_table")
data class Hike(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val date: Date,
    val duration: Long, // in milliseconds
    val distance: Float, // in meters
    val averageSpeed: Float, // in m/s
    val maxSpeed: Float, // in m/s
    val elevationGain: Float, // in meters
    val maxElevation: Double,
    val minElevation: Double,
    val calories: Int,
    val steps: Int = 0,
    val imagePath: String? = null
)