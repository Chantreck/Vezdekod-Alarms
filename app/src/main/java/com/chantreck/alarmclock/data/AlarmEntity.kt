package com.chantreck.alarmclock.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarm")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val time: String,
    val days: String,
    val enabled: Boolean = true
)