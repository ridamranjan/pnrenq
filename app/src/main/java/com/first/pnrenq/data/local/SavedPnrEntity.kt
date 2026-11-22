
package com.first.pnrenq.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_pnrs")
data class SavedPnrEntity(
    @PrimaryKey
    val pnrNumber: String,
    val trainNumber: String,
    val trainName: String,
    val journeyDate: String,
    val fromStation: String,
    val toStation: String,
    val bookingStatus: String,
    val currentStatus: String,
    val chartStatus: String?,
    val lastCheckedAtMillis: Long,
    val createdAtMillis: Long
)
