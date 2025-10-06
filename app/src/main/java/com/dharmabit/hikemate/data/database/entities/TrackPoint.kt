package com.dharmabit.hikemate.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "track_point_table")
data class TrackPoint(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val hikeId: Long,
    val latitude: Double,
    val longitude: Double,
    val altitude: Double,
    val timestamp: Date,
    val speed: Float,
    val accuracy: Float
)