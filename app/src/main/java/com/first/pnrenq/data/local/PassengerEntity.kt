
package com.first.pnrenq.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "passengers",
    foreignKeys = [
        ForeignKey(
            entity = SavedPnrEntity::class,
            parentColumns = ["pnrNumber"],
            childColumns = ["pnrNumber"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["pnrNumber"]),
        Index(
            value = ["pnrNumber", "passengerIndex"],
            unique = true
        )
    ]
)
data class PassengerEntity(
    @PrimaryKey(autoGenerate = true)
    val passengerId: Long = 0,
    val pnrNumber: String,
    val passengerIndex: Int,
    val bookingStatus: String,
    val currentStatus: String,
    val coach: String?,
    val berth: String?
)
